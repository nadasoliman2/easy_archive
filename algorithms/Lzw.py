def compress(data):
    dictionary_size = 128
    dictionary = dict((chr(i), i) for i in range(dictionary_size))
    w = ""
    result = []
    for c in data:
        wc = w + c
        if wc in dictionary:
            w = wc
        else:
            result.append(dictionary[w])
            dictionary[wc] = dictionary_size
            dictionary_size += 1
            w = c
    if w:
        result.append(dictionary[w])
    return result
def decompress(codes):
    dictionary_size = 128
    dictionary = dict((i, chr(i)) for i in range(dictionary_size))
    w = chr(codes.pop(0))
    result = w
    for k in codes:
        if k in dictionary:
            entry = dictionary[k]
        elif k == dictionary_size:
            entry = w + w[0]
        else:
            raise ValueError('Bad compressed k: %s' % k)
        result += entry
        dictionary[dictionary_size] = w + entry[0]
        dictionary_size += 1
        w = entry
    return result
data = "ABAABABBAABAABAAAABABBBBBBBB"
compressed_data = compress(data)
print(compressed_data)

decompressed_data = decompress(compressed_data)
print(decompressed_data)