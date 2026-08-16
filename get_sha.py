import re
import codecs

data = codecs.open('out.txt', 'r', 'utf-16le').read()
sha1_match = re.search(r'SHA1:\s+([0-9A-F:]+)', data)
sha256_match = re.search(r'SHA256:\s+([0-9A-F:]+)', data)

with open('clean_shas.txt', 'w') as f:
    if sha1_match:
        f.write('SHA1: ' + sha1_match.group(1).strip() + '\n')
    if sha256_match:
        f.write('SHA256: ' + sha256_match.group(1).strip() + '\n')
