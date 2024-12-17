import heapq

class Node:
    def __init__(self, freq, byte, left=None, right=None):
        self.freq = freq
        self.byte = byte
        self.left = left
        self.right = right
        self.huff = ''
    
    def __lt__(self, other):
        return self.freq < other.freq
    
    def __eq__(self, other):
        return self.freq == other.freq


class HuffmanCoding:    
    """Algorithm Intuition

    1. calculate the frequency of each character in the string
    2. create a leaf node for each character and its frequency
    3. create a min heap of all leaf nodes (priority queue)
    4. while heap has more than one node:
        a. remove two nodes with the lowest frequencies from the heap
        b. create a new internal node with these two nodes as children and with frequency equal to the sum of the two nodes' frequencies
        c. insert the new node back into the heap

    5. the remaining node is the root node and the tree is complete
    6. assign codes to each byte (symbol) by traversing the tree from the root to the byte (symbol)
    """

    def __init__(self, type, path = None, str = None):
        self.path = path
        self.type = type
        self.str = str
        self.heap = []
        self.codes = {}
        self.reverse_mapping = {}

        self.bytes = []
        self.frequency = {}

    def __read_file_byte_by_byte(self, path):
        try:
            with open(path, 'rb') as file:
                while True:
                    byte = file.read(1)
                    if not byte:
                        break
                    self.bytes.append(int.from_bytes(byte, byteorder='big'))
            return self.bytes
        except Exception as e:
            print(f"Reading file error: {e}")

    def ___represent_tree(self, file):
        
        assert self.type == 'file' , "Works only for files"
        assert self.path is not None, "Path is not provided"
        assert len(self.codes) > 0, "No codes generated"

        num_codes = len(self.codes)

        file.write(num_codes.to_bytes(4, byteorder='big'))

        for byte, code in self.codes.items():
            file.write(bytes([byte]))
            file.write(len(code).to_bytes(1, byteorder='big'))
            file.write(int(code, 2).to_bytes((len(code) + 7) // 8, byteorder='big'))

        return self
    
    def ___represent_tree_string(self):

        assert self.type == 'text' , "Works only for strings"
        assert self.str is not None, "String is not provided"
        assert len(self.codes) > 0, "No codes generated"

        num_codes = len(self.codes)
        return f"{num_codes} {''.join([f'{byte} {len(code)} {code}' for byte, code in self.codes.items()])}"

    def ___padding_data(self, file):
        encoded_data = ""

        for _, code in self.codes.items():
            encoded_data += code
        padding_length = 8 - (len(encoded_data) % 8)
        encoded_data += "0" * padding_length

        file.write(padding_length.to_bytes(1, byteorder='big'))
        for i in range(0, len(encoded_data), 8):
            byte = encoded_data[i: i+8]
            file.write(int(byte, 2).to_bytes(1, byteorder='big'))

        return self
    
    def ___padding_data_string(self):
        encoded_data = ""

        for _, code in self.codes.items():
            encoded_data += code
        padding_length = 8 - (len(encoded_data) % 8)
        encoded_data += "0" * padding_length

        return f"{padding_length} {''.join([encoded_data[i: i+8] for i in range(0, len(encoded_data), 8)])}"

    def __write_file(self, path):
        """
        
        Keyword arguments:
        path : str -> path to the file


        1. first 4 bytes the size of the tree
        2. next bytes for each code:
            1. byte for the byte itself
            2. byte for the length of the code
            3. bytes for the code
        3. next byte for the length of the padding
        4. remaning bytes are for the compressed data

        Return: HuffmanCoding object
        """
        try:
            with open(path, 'wb') as file:
                self.___represent_tree(file)
                self.___padding_data(file)
        except Exception as e:
            print(e)
        finally:
            return self
        
    def __write_file_string(self):
        return f"{self.___represent_tree_string()} {self.___padding_data_string()}"
        
    def __make_frequency_dict(self, bytes):
        frequency = {}
        for character in bytes:
            if not character in frequency:
                frequency[character] = 0
            frequency[character] += 1
        return frequency
    
    def __create_leaf_nodes(self, frequency):
        heap = []
        for byte in frequency:
            node = Node(frequency[byte], byte)
            heapq.heappush(heap, node)
        return heap
    
    def __build_huffman_tree(self, heap):
        while len(heap) > 1:
            node_1 = heapq.heappop(heap)
            node_2 = heapq.heappop(heap)

            merged_freq = node_1.freq + node_2.freq
            merged_node = Node(merged_freq, None, node_1, node_2)
            heapq.heappush(heap, merged_node)
        return heap
    
    def __build_codes(self, heap, code = ''):
        if heap is not None:

            if heap.byte is not None:  
                self.codes[heap.byte] = code
                return

            self.__build_codes(heap.left, code + '0')
            self.__build_codes(heap.right, code + '1')
            

    def __compress_text(self, text):
        self.frequency = self.__make_frequency_dict(text)
        self.heap = self.__create_leaf_nodes(self.frequency)
        self.heap = self.__build_huffman_tree(self.heap)

        self.__build_codes(self.heap[0])

        return self.__write_file_string()
 
    def __compress_file(self, path):
        self.bytes = self.__read_file_byte_by_byte(self.path)
        self.frequency = self.__make_frequency_dict(self.bytes)
        self.heap = self.__create_leaf_nodes(self.frequency)
        self.heap = self.__build_huffman_tree(self.heap)

        self.__build_codes(self.heap[0])

        self.__write_file('compressed.bin')

        return self.codes
 


    def compress(self) -> list:
        if self.type == 'text':
            return self.__compress_text(self.str)
        elif self.type == 'file':
            return self.__compress_file(self.path)
           


    def decompress(self, heap):
        """heap is the reconstructed tree from compressed file"""
        pass





if __name__ == '__main__':
    # huffman = HuffmanCoding('file', 'file.pdf')
    huffman = HuffmanCoding('text', str='Hello world')
    huf_codes = huffman.compress()

    print(huf_codes)

    # for byte, code in huf_codes.items():
        # print(f"byte: {byte}, code: {code}")
    
