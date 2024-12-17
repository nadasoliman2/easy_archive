import java.io.*;
import java.nio.file.Files;
import java.util.*;


interface CompressionAlgorithm {
    byte[] compress(byte[] input) throws IOException;
    byte[] decompress(byte[] compressed) throws IOException;
    String getAlgorithmName();
}


class ArithmeticCompressionAlgorithm implements CompressionAlgorithm {
    @Override
    public byte[] compress(byte[] input) throws IOException {
        ByteArrayOutputStream compressedData = new ByteArrayOutputStream();
        
        Map<Byte, Integer> frequencyMap = new HashMap<>();
        for (byte b : input) {
            frequencyMap.put(b, frequencyMap.getOrDefault(b, 0) + 1);
        }
        
        compressedData.write(frequencyMap.size());
        for (Map.Entry<Byte, Integer> entry : frequencyMap.entrySet()) {
            compressedData.write(entry.getKey());
            compressedData.write(entry.getValue());
        }
        
        for (byte b : input) {
            compressedData.write((int)(Math.log(frequencyMap.get(b)) * 10));
        }
        
        return compressedData.toByteArray();
    }

    @Override
    public byte[] decompress(byte[] compressed) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressed);
        ByteArrayOutputStream decompressedData = new ByteArrayOutputStream();
        
        int frequencyMapSize = inputStream.read();
        Map<Byte, Integer> frequencyMap = new HashMap<>();
        
        for (int i = 0; i < frequencyMapSize; i++) {
            byte key = (byte)inputStream.read();
            int frequency = inputStream.read();
            frequencyMap.put(key, frequency);
        }
        
        int b;
        while ((b = inputStream.read()) != -1) {
            for (Map.Entry<Byte, Integer> entry : frequencyMap.entrySet()) {
                if ((int)(Math.log(entry.getValue()) * 10) == b) {
                    decompressedData.write(entry.getKey());
                    break;
                }
            }
        }
        
        return decompressedData.toByteArray();
    }

    @Override
    public String getAlgorithmName() {
        return "Arithmetic Coding Algorithm";
    }
}


class RunLengthCompressionAlgorithm implements CompressionAlgorithm {
    @Override
    public byte[] compress(byte[] input) throws IOException {
        ByteArrayOutputStream compressedData = new ByteArrayOutputStream();
        
        for (int i = 0; i < input.length; i++) {
            int count = 1;
            while (i + 1 < input.length && input[i] == input[i + 1] && count < 255) {
                count++;
                i++;
            }
            compressedData.write(input[i]);
            compressedData.write(count);
        }
        
        return compressedData.toByteArray();
    }

    @Override
    public byte[] decompress(byte[] compressed) throws IOException {
        ByteArrayOutputStream decompressedData = new ByteArrayOutputStream();
        
        for (int i = 0; i < compressed.length; i += 2) {
            byte value = compressed[i];
            int count = compressed[i + 1];
            
            for (int j = 0; j < count; j++) {
                decompressedData.write(value);
            }
        }
        
        return decompressedData.toByteArray();
    }

    @Override
    public String getAlgorithmName() {
        return "Run-Length Encoding Algorithm";
    }
}


class HuffmanCompressionAlgorithm implements CompressionAlgorithm {
    private static class HuffmanNode implements Comparable<HuffmanNode> {
        byte data;
        int frequency;
        HuffmanNode left, right;

        HuffmanNode(byte data, int frequency) {
            this.data = data;
            this.frequency = frequency;
        }

        @Override
        public int compareTo(HuffmanNode other) {
            return Integer.compare(this.frequency, other.frequency);
        }
    }

    @Override
    public byte[] compress(byte[] input) throws IOException {
        Map<Byte, Integer> frequencyMap = new HashMap<>();
        for (byte b : input) {
            frequencyMap.put(b, frequencyMap.getOrDefault(b, 0) + 1);
        }

        PriorityQueue<HuffmanNode> minHeap = new PriorityQueue<>();
        for (Map.Entry<Byte, Integer> entry : frequencyMap.entrySet()) {
            minHeap.offer(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        while (minHeap.size() > 1) {
            HuffmanNode left = minHeap.poll();
            HuffmanNode right = minHeap.poll();
            HuffmanNode parent = new HuffmanNode((byte)0, left.frequency + right.frequency);
            parent.left = left;
            parent.right = right;
            minHeap.offer(parent);
        }

        Map<Byte, String> huffmanCodes = new HashMap<>();
        generateHuffmanCodes(minHeap.peek(), "", huffmanCodes);

        StringBuilder compressedBits = new StringBuilder();
        for (byte b : input) {
            compressedBits.append(huffmanCodes.get(b));
        }

        ByteArrayOutputStream compressedData = new ByteArrayOutputStream();
        int paddingLength = (8 - (compressedBits.length() % 8)) % 8;
        compressedData.write(paddingLength);

        compressedData.write(frequencyMap.size());
        for (Map.Entry<Byte, Integer> entry : frequencyMap.entrySet()) {
            compressedData.write(entry.getKey());
            compressedData.write(entry.getValue());
        }

        for (int i = 0; i < compressedBits.length(); i += 8) {
            String byteString = compressedBits.substring(
                i, 
                Math.min(i + 8, compressedBits.length())
            );
            compressedData.write(Integer.parseInt(byteString, 2));
        }

        return compressedData.toByteArray();
    }

    @Override
    public byte[] decompress(byte[] compressed) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressed);
        
        int paddingLength = inputStream.read();
        
        int frequencyMapSize = inputStream.read();
        Map<Byte, Integer> frequencyMap = new HashMap<>();
        
        for (int i = 0; i < frequencyMapSize; i++) {
            byte key = (byte)inputStream.read();
            int frequency = inputStream.read();
            frequencyMap.put(key, frequency);
        }

        PriorityQueue<HuffmanNode> minHeap = new PriorityQueue<>();
        for (Map.Entry<Byte, Integer> entry : frequencyMap.entrySet()) {
            minHeap.offer(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        while (minHeap.size() > 1) {
            HuffmanNode left = minHeap.poll();
            HuffmanNode right = minHeap.poll();
            HuffmanNode parent = new HuffmanNode((byte)0, left.frequency + right.frequency);
            parent.left = left;
            parent.right = right;
            minHeap.offer(parent);
        }

        HuffmanNode root = minHeap.peek();
        
        ByteArrayOutputStream decompressedData = new ByteArrayOutputStream();
        
        StringBuilder compressedBits = new StringBuilder();
        int b;
        while ((b = inputStream.read()) != -1) {
            String bitString = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            compressedBits.append(bitString);
        }

        compressedBits.setLength(compressedBits.length() - paddingLength);

        HuffmanNode current = root;
        for (int i = 0; i < compressedBits.length(); i++) {
            current = (compressedBits.charAt(i) == '0') ? current.left : current.right;
            
            if (current.left == null && current.right == null) {
                decompressedData.write(current.data);
                current = root;
            }
        }

        return decompressedData.toByteArray();
    }

    private void generateHuffmanCodes(HuffmanNode node, String code, Map<Byte, String> huffmanCodes) {
        if (node == null) return;
        
        if (node.left == null && node.right == null) {
            huffmanCodes.put(node.data, code);
            return;
        }
        
        generateHuffmanCodes(node.left, code + "0", huffmanCodes);
        generateHuffmanCodes(node.right, code + "1", huffmanCodes);
    }

    @Override
    public String getAlgorithmName() {
        return "Huffman Coding Algorithm";
    }
}

class GolombCompressionAlgorithm implements CompressionAlgorithm {
    @Override
    public byte[] compress(byte[] input) throws IOException {
        ByteArrayOutputStream compressedData = new ByteArrayOutputStream();
        
        int divisor = 4;
        
        for (byte b : input) {
            int value = b & 0xFF;  
            int quotient = value / divisor;
            int remainder = value % divisor;
            
            for (int i = 0; i < quotient; i++) {
                compressedData.write(1);
            }
            compressedData.write(0);
            
            for (int i = divisor / 2; i > 0; i /= 2) {
                compressedData.write((remainder & i) != 0 ? 1 : 0);
            }
        }
        
        return compressedData.toByteArray();
    }

    @Override
    public byte[] decompress(byte[] compressed) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressed);
        ByteArrayOutputStream decompressedData = new ByteArrayOutputStream();
        
        int divisor = 4;
        
        while (inputStream.available() > 0) {
            int quotient = 0;
            int bit;
            while ((bit = inputStream.read()) == 1) {
                quotient++;
            }
            
            int remainder = 0;
            for (int i = divisor / 2; i > 0; i /= 2) {
                bit = inputStream.read();
                if (bit == 1) {
                    remainder += i;
                }
            }
            
            int value = quotient * divisor + remainder;
            decompressedData.write(value);
        }
        
        return decompressedData.toByteArray();
    }

    @Override
    public String getAlgorithmName() {
        return "Golomb Code Algorithm";
    }
}

class LZWCompressionAlgorithm implements CompressionAlgorithm {
    @Override
    public byte[] compress(byte[] input) throws IOException {
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(String.valueOf((char)i), i);
        }
        
        ByteArrayOutputStream compressedData = new ByteArrayOutputStream();
        String currentSequence = "";
        int dictionarySize = 256;
        
        for (byte b : input) {
            String newSequence = currentSequence + (char)(b & 0xFF);
            
            if (dictionary.containsKey(newSequence)) {
                currentSequence = newSequence;
            } else {
                int code = dictionary.get(currentSequence);
                compressedData.write((code >> 8) & 0xFF);  
                compressedData.write(code & 0xFF);         
                
                dictionary.put(newSequence, dictionarySize++);
                currentSequence = String.valueOf((char)(b & 0xFF));
            }
        }
        
        if (!currentSequence.isEmpty()) {
            int code = dictionary.get(currentSequence);
            compressedData.write((code >> 8) & 0xFF);  
            compressedData.write(code & 0xFF);         
        }
        
        return compressedData.toByteArray();
    }

    @Override
    public byte[] decompress(byte[] compressed) throws IOException {
        Map<Integer, String> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(i, String.valueOf((char)i));
        }
        
        ByteArrayOutputStream decompressedData = new ByteArrayOutputStream();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressed);
        
        int dictionarySize = 256;
        int previousCode = (inputStream.read() << 8) | inputStream.read();
        String currentSequence = dictionary.get(previousCode);
        decompressedData.write(currentSequence.getBytes());
        
        while (inputStream.available() > 0) {
            int currentCode = (inputStream.read() << 8) | inputStream.read();
            String entry;
            
            if (dictionary.containsKey(currentCode)) {
                entry = dictionary.get(currentCode);
            } else if (currentCode == dictionarySize) {
                entry = currentSequence + currentSequence.charAt(0);
            } else {
                throw new IOException("Bad compressed k");
            }
            
            decompressedData.write(entry.getBytes());
            
            dictionary.put(dictionarySize++, currentSequence + entry.charAt(0));
            
            currentSequence = entry;
        }
        
        return decompressedData.toByteArray();
    }

    @Override
    public String getAlgorithmName() {
        return "Lempel-Ziv-Welch Algorithm";
    }
}

public class FileCompressor {
    public void compress(String algorithmName, File file, String compressedPath) throws IOException {
        
        switch (algorithmName) {
            case "Arithmetic Algorithm":
                ArithmeticCompressionAlgorithm arithmetic = new ArithmeticCompressionAlgorithm();
                byte[] arithmeticCompressed = arithmetic.compress(Files.readAllBytes(file.toPath()));
                Files.write(new File(compressedPath).toPath(), arithmeticCompressed);
                break;
            case "Run-Length Encoding Algorithm":
                RunLengthCompressionAlgorithm rle = new RunLengthCompressionAlgorithm();
                byte[] rleCompressed = rle.compress(Files.readAllBytes(file.toPath()));
                Files.write(new File(compressedPath).toPath(), rleCompressed);
                break;
            case "Huffman Coding Algorithm":
                HuffmanCompressionAlgorithm huffman = new HuffmanCompressionAlgorithm();
                byte[] huffmanCompressed = huffman.compress(Files.readAllBytes(file.toPath()));
                Files.write(new File(compressedPath).toPath(), huffmanCompressed);
                break;
            case "Golomb code alggorithm":
                GolombCompressionAlgorithm golomb = new GolombCompressionAlgorithm();
                byte[] golombCompressed = golomb.compress(Files.readAllBytes(file.toPath()));
                Files.write(new File(compressedPath).toPath(), golombCompressed);                
                break;
            case "Lempel-Ziv-Welch Algorithm":
                LZWCompressionAlgorithm lzw = new LZWCompressionAlgorithm();
                byte[] lzwCompressed = lzw.compress(Files.readAllBytes(file.toPath()));
                Files.write(new File(compressedPath).toPath(), lzwCompressed);
                break;
            default:
                throw new IllegalArgumentException("Invalid algorithm name");
        } 

    }
}

