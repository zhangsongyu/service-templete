package mesosphere.jackson

import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.deser.CreatorProperty
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator
import mesosphere.reflect.{CaseClassFactory, CompanionMetadata}

import scala.collection.JavaConverters._
import scala.collection.immutable.ListMap
import scala.reflect.runtime.universe._

protected class CaseClassValueInstantiator(
                                            config: DeserializationConfig,
                                            beanDesc: BeanDescription)
  extends StdValueInstantiator(config, beanDesc.getType) {

  import mesosphere.reflect.classLoaderMirror

  val classSymbol = classLoaderMirror.classSymbol(beanDesc.getBeanClass)

  assert(
    classSymbol.isCaseClass,
    "%s: refusing to instantiate non-case-class type [%s]".format(
      getClass.getName,
      beanDesc.getBeanClass.getName
    )
  )

  /**
    * Contains `Some(value)` if there is a default value for the associated
    * parameter name for the supplied case class type and `None` otherwise.
    */
  private[this] lazy val defaultArguments: ListMap[String, Option[() => Any]] = {
    val companion = CompanionMetadata(classSymbol).get

    val applySymbol: MethodSymbol = {
      val symbol = companion.classType.member("apply": TermName)
      if (symbol.isMethod) symbol.asMethod
      else symbol.asTerm.alternatives.head.asMethod // symbol.isTerm
    }

    def valueFor(i: Int): Option[() => Any] = {
      val defaultThunkName = s"apply$$default$$${i + 1}": TermName
      val defaultThunkSymbol = companion.classType member defaultThunkName

      if (defaultThunkSymbol == NoSymbol) None
      else {
        val defaultThunk =
          companion.instanceMirror reflectMethod defaultThunkSymbol.asMethod
        Some(() => defaultThunk.apply())
      }
    }

    ListMap(applySymbol.paramLists.flatten.zipWithIndex.map {
      case (p, i) =>
        p.name.toString -> valueFor(i)
    }: _*)
  }

  private[this] lazy val factory =
    new CaseClassFactory(beanDesc.getBeanClass)

  private[this] lazy val ctorProps = for {
    prop <- beanDesc.findProperties().asScala
    param <- Option(prop.getConstructorParameter)
    name = new PropertyName(prop.getName)
    wrapperName = prop.getWrapperName
    idx = param.getIndex
    javaType: JavaType = param.getType
  } yield {
    new CreatorProperty(
      name,
      javaType,
      wrapperName,
      null, //config.findTypeDeserializer(beanDesc.getType),
      null,
      param,
      idx,
      null,
      PropertyMetadata.STD_REQUIRED_OR_OPTIONAL
    )
  }

  val creator = beanDesc.getConstructors.asScala.headOption

  configureFromObjectSettings(
    null, null, null, null,
    creator.orNull,
    ctorProps.toArray
  )

  override def createFromObjectWith(cxt: DeserializationContext,
                                    args: Array[Object]): Object = {
    val params: Seq[_] = (args.toSeq zip defaultArguments.values).map {
      case (None, Some(default)) =>
        default.apply
      case (null, Some(default)) =>
        default.apply
      case (a, Some(default)) if a == 0 =>
        default.apply
      case (deserialized, d) =>
        deserialized
    }
    factory.buildWith(params.toArray.toSeq).asInstanceOf[Object]
  }
}
