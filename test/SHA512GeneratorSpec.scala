import helpers.SHA512Generator
import org.specs2.mutable._

class SHA512GeneratorSpec extends Specification
{
  val sha512Of123456 = "ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413"

  "SHA512Generator" should {
    "generate " + sha512Of123456 + " for 123456" in {
      SHA512Generator.generate("123456") mustEqual sha512Of123456
    }
  }
}