package queuedserver;
 
import java.math.BigInteger;
import java.util.Random;
 
public class RSA {
    private BigInteger p;
    private BigInteger q;
    private BigInteger N;
    private BigInteger phi;
    private BigInteger e;
    private BigInteger d;
    private int bitlength = 256;
    private Random     r;
 
    public RSA() {
        r = new Random();
        p = BigInteger.probablePrime(bitlength, r);
        q = BigInteger.probablePrime(bitlength, r);
        N = p.multiply(q);
        phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        e = BigInteger.probablePrime(bitlength / 2, r);
        while (phi.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(phi) < 0) {
            e.add(BigInteger.ONE);
        }
        d = e.modInverse(phi);
    }
 
    // Encrypt message
    public BigInteger encrypt(BigInteger message) {
        return ((message)).modPow(e, N);
    }
 
    // Decrypt message
    public BigInteger decrypt(BigInteger message) {
        return ((message)).modPow(d, N);
    }
    
    public BigInteger[] getPubKey() {
        BigInteger pubKey[] = {e,N};
        return pubKey;
    }
}