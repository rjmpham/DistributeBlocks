import java.security.*;

public class Wallet {

	public PrivateKey privateKey;
	public PublicKey publicKey;

	public Wallet(){
	  KeyPair pair = Crypto.keyPairGenerator();
    privateKey = pair.getPrivate();
    publicKey = pair.getPublic();
	}

}
