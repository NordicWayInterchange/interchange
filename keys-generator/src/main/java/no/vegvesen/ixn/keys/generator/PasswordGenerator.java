package no.vegvesen.ixn.keys.generator;


import java.security.SecureRandom;

public sealed interface PasswordGenerator {
    String generatePassword();

    static PasswordGenerator random(SecureRandom random, int length) {

        final char[] allowedChars = {
                'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
                '0','1','2','3','4','5','6','7','8','9',
                '*','-','_','$','+'
        };

        return new RandomPasswordGenerator(length, allowedChars, random);
    }

    static PasswordGenerator staticPassword(final String password) {
        return new StaticPasswordGenerator(password);
    }

    final class RandomPasswordGenerator implements PasswordGenerator {

        private final int length;
        private final char[] allowedChars;
        private final SecureRandom random;

        public RandomPasswordGenerator(int length, char[] allowedChars, SecureRandom random) {
            this.length = length;
            this.allowedChars = allowedChars;
            this.random = random;
        }

        @Override
        public String generatePassword() {

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                builder.append(allowedChars[random.nextInt(allowedChars.length)]);
            }
            return builder.toString();
        }
    }

    final class StaticPasswordGenerator implements PasswordGenerator {
        private final String password;

        public StaticPasswordGenerator(String password) {
            this.password = password;
        }

        @Override
        public String generatePassword() {
            return password;
        }
    }
}


