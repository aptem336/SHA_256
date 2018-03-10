package hash;

import java.nio.charset.StandardCharsets;

public class Hash {

    private final static int H[] = {
        0x6a09e667,
        0xbb67ae85,
        0x3c6ef372,
        0xa54ff53a,
        0x510e527f,
        0x9b05688c,
        0x1f83d9ab,
        0x5be0cd19
    };
    private final static int[] K = {
        0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5,
        0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
        0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3,
        0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
        0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc,
        0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
        0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7,
        0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
        0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13,
        0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
        0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3,
        0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
        0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5,
        0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
        0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208,
        0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2};

    public static void main(String[] args) {
        calcHash(getTextBytes("3.141592653589793"));
    }

    private static byte[] getTextBytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    private static void calcHash(byte[] input) {
        System.out.println("");
        System.out.println("===========================================================================================================================================");
        System.out.println("Data:");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("HEX");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------");
        for (int i = 0; i < bytes.length; i++) {
            System.out.print(trimToLen(toHex(bytes[i] & 0xFF), 2) + ":");
            if ((i + 1) % 4 == 0) {
                System.out.println("");
            }
        }
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("BIN");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------");
        for (int i = 0; i < bytes.length; i++) {
            System.out.print(trimToLen(toBin(bytes[i] & 0xFF), 8) + " ");
            if ((i + 1) % 4 == 0) {
                System.out.println("");
            }
        }
        //Инициализируем необходимые регистры
        int[] reg = new int[8];
        System.arraycopy(H, 0, reg, 0, 8);
        int[] hex = new int[8];
        System.arraycopy(H, 0, hex, 0, 8);
        //Количество раз равное длине цепочки / 64
        for (int i = 0; i < bytes.length / 64; i++) {
            //Берём цепочку по 64 байта - 512 бит
            byte[] bytes64 = new byte[64];
            System.arraycopy(bytes, i * 64, bytes64, 0, 64);
            //Получаем слова
            int[] words = buildWords(bytes64);
            //Итерации вычисления
            for (int j = 0; j < 64; j++) {
                reg = iterate(reg, K[j], words[j]);
            }
            for (int j = 0; j < 8; j++) {
                hex[j] += reg[j];
            }
        }
        System.out.println("===========================================================================================================================================");
        System.out.println("Hash:");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("HEX");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------");
        for (int i = 0; i < hex.length; i++) {
            System.out.println(addDelimer(trimToLen(toHex(hex[i] & 0xFFFFFFFF), 8), 2, ":"));
        }
        System.out.println("");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("BIN");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------");
        for (int i = 0; i < hex.length; i++) {
            System.out.println(addDelimer(trimToLen(toBin(hex[i] & 0xFFFFFFFF), 32), 8, " "));
        }
        System.out.println("===========================================================================================================================================");
    }

    private static int[] iterate(int[] reg, int k, int w) {
        int s0 = (Integer.rotateRight(reg[0], 2)) ^ (Integer.rotateRight(reg[0], 13)) ^ (Integer.rotateRight(reg[0], 22));
        int s1 = (Integer.rotateRight(reg[4], 6)) ^ (Integer.rotateRight(reg[4], 11)) ^ (Integer.rotateRight(reg[4], 25));
        int majority = (reg[0] & reg[1]) ^ (reg[0] & reg[2]) ^ (reg[1] & reg[2]);
        int choice = (reg[4] & reg[5]) ^ ((~reg[4]) & reg[6]);
        int temp1 = reg[7] + s1 + choice + k + w;
        int temp2 = s0 + majority;
        reg[7] = reg[6];//h = g
        reg[6] = reg[5];//g = f
        reg[5] = reg[4];//f = e
        reg[4] = reg[3] + temp1;//e = ...
        reg[3] = reg[2];//d = c
        reg[2] = reg[1];//c = b
        reg[1] = reg[0];//b = a
        reg[0] = temp1 + temp2;//a = ...
        return reg;
    }

    private static int[] buildWords(byte[] bytes64) {
        int[] words = new int[64];
        //Формируем слова из сообщения
        for (int j = 0; j < 16; j++) {
            words[j] = 0;
            for (int k = 0; k < 4; k++) {
                words[j] += (bytes64[j * 4 + k] & 0xFF) << ((3 - k) * 8);
            }
        }
        //Генерируем ещё 48 слов
        for (int j = 16; j < 64; j++) {
            words[j] = 0;
            int s0 = (Integer.rotateRight(words[j - 15], 7)) ^ (Integer.rotateRight(words[j - 15], 18)) ^ (words[j - 15] >>> 3);
            int s1 = (Integer.rotateRight(words[j - 2], 17)) ^ (Integer.rotateRight(words[j - 2], 19)) ^ (words[j - 2] >>> 10);
            words[j] = words[j - 16] + s0 + words[j - 7] + s1;
        }
        return words;
    }

    private static byte[] buildBytes(byte[] inputBytes) {
        int inputLength = inputBytes.length;
        //Тут сложная формула для получения длины массива, простыми словами, давляем место для длины, единицы и нулевых битов
        byte[] bytes = new byte[inputLength + 8 + (1 + (448 - ((inputLength * 8 + 1) % 512)) / 8)];
        //Копируем исходный массив
        System.arraycopy(inputBytes, 0, bytes, 0, inputLength);
        //Дописываем единичный бит
        bytes[inputLength] += 1 << 7;
        //Дописываем 64-х битную длину
        for (int i = bytes.length - 4; i < bytes.length; i++) {
            bytes[i] += ((0xFF << ((bytes.length - i - 1) * 8)) & inputLength * 8) >> ((bytes.length - i - 1) * 8);
            bytes[i] &= 0xFF;
        }
        return bytes;
    }

    private static String toBin(int number) {
        return Integer.toBinaryString(number);
    }

    private static String toHex(int number) {
        return Integer.toHexString(number).toUpperCase();
    }

    private static String addDelimer(String string, int position, String delimer) {
        return string.replaceAll("(.{" + position + "})", "$1" + delimer);
    }

    private static String trimToLen(String string, int length) {
        return String.format("%" + length + "s", string).replace(' ', '0');
    }
}
