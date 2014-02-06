package helpers

/**
 * A helper object for creating secure hashes of 128 characters using SHA-512
 */
object SHA512Generator
{
  /**
   * Generates the SHA-512 hashed value of given String
   *
   * @param s A value whose hash will be generated
   *
   * @return Generated hash value
   */
  def generate(s: String): String = {
    val md = java.security.MessageDigest.getInstance("SHA-512")
    val bytes = md.digest(s.getBytes)

    val sb: StringBuilder = new StringBuilder()
    for(b <- bytes) sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1))

    sb.toString()
  }
}