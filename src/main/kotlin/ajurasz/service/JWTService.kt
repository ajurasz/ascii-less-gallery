package ajurasz.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.*
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter

class JWTService {

    companion object {
        val SECRET: ByteArray = DatatypeConverter.parseBase64Binary(System.getenv("secret") ?: "top-secret-change-it")
    }

    fun create(email: String): String {
        val signatureAlgorithm = SignatureAlgorithm.HS256
        val signingKey = SecretKeySpec(SECRET, signatureAlgorithm.jcaName)

        return Jwts.builder()
                .setIssuedAt(Date())
                .setIssuer(email)
                .signWith(signatureAlgorithm, signingKey)
                .compact()
    }

    fun getEmail(jwt: String): String {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(jwt)
                .body
                .issuer
    }
}