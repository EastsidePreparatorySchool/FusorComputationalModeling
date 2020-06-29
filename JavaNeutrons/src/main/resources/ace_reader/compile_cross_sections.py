import os.path
import glob

from pyne import ace
print glob.glob('../data/ace/1001.*')
libFile = ace.Library('../data/ace/1001.800nc.ace')
libFile.read()
print libFile.tables
table = libFile.tables['1001.800nc']
# print table.energy
# print table.sigma_a
# print table.sigma_t
print table.reactions
print table.reactions[2]


def make_csv(t):
    return '\n'.join([(str(e*1e6)+','+str(s)+','+str(a)+','+str(total)) for (e, s, a, total) in zip(t.energy, t.reactions[2].sigma, t.sigma_a, t.sigma_t)]) + '\n'

def write_reaction_csv(directory, t, mat):
    with open(os.path.join(directory, mat+'.csv'), 'w') as f:
        f.write("energy (eV), scatter (barn), absorb (barn), total (barn)\n")
        f.write(make_csv(t))

write_reaction_csv('.', table, '1001')

for filename in glob.glob('../data/ace/*'):
    mat = os.path.basename(filename)
    try:
        libFile = ace.Library(filename)
        libFile.read()
        values_view = libFile.tables.values()
        value_iterator = iter(values_view)
        table = next(value_iterator)

        write_reaction_csv('../data/ace', table, mat)
    except ValueError:
        print('ac_reader failed to parse '+filename)
