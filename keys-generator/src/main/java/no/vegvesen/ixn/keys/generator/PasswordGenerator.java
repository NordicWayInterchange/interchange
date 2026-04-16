package no.vegvesen.ixn.keys.generator;


import java.security.SecureRandom;

public interface PasswordGenerator {
    String generatePassword();

    static PasswordGenerator random(SecureRandom random, int length) {

        final char[] allowedChars = {
                'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
                '0','1','2','3','4','5','6','7','8','9',
                '*','-','_','$','+'
        };

        return () -> {
            StringBuilder builder = new StringBuilder();
            for (int i =  0; i < length; i++) {
                builder.append(allowedChars[random.nextInt(allowedChars.length)]);
            }
            return builder.toString();
        };
    }
}


