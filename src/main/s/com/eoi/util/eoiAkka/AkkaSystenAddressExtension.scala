package s.com.eoi.util.eoiAkka

import akka.actor.{ExtendedActorSystem, Extension, ExtensionKey}

class AkkaAddressExtensionImpl(system: ExtendedActorSystem) extends Extension {
  //得到集群的address
  def address = system.provider.getDefaultAddress
}

object AkkaAddressExtension extends ExtensionKey[AkkaAddressExtensionImpl]