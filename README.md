# Árvores de busca com dados de tornados (BST × AVL × Rubro-Negra)

Trabalho 1 de **Estrutura de Dados** — Engenharia de Computação, IFC Campus São Bento do Sul (Prof. Lucas Dominguez Cordeiro).

**Integrantes:** Luan Henrique de Souza e Ana Luiza Lemos Corrêa

O programa implementa três árvores de busca (binária de busca simples, AVL e Rubro-Negra), mede o desempenho delas com um dataset real de tornados dos EUA e mostra os resultados em uma interface gráfica (Swing), com gráficos de comparação e uma aba para operar as árvores ao vivo.

---

## 1. Requisitos

- **JDK 11 ou superior** (testado com JDK 21 e JDK 25). Basta ter `javac` e `java` no PATH.
- Nenhuma biblioteca externa: os gráficos são desenhados com Java2D/Swing.

Confira com:

```
javac -version
java -version
```

## 2. Como rodar

Todos os comandos devem ser executados **na pasta do projeto** (a que contém `src`, `data` e `results`), porque os caminhos de `data/` e `results/` são relativos. No Linux/macOS, troque `\` por `/`.

### 2.1 Benchmarks (gera os CSVs em `results/`)

```
javac -encoding UTF-8 -d out -sourcepath src src\Main.java
java -cp out Main
```

Carrega `data/tornado_full.csv` (74.956 registros), insere os dados nas três árvores e mede altura, comparações e tempo. Leva cerca de 1 minuto. Gera 10 arquivos em `results/`:

| Arquivo | Conteúdo |
|---|---|
| `altura_bst.csv`, `altura_avl.csv`, `altura_rbt.csv` | altura da árvore a cada 1.000 elementos inseridos |
| `comparacoes_*.csv` | comparações de chaves acumuladas a cada 1.000 inserções |
| `tempo_*.csv` | tempo de inserção (acumulado, do bloco e médio por inserção) |
| `operacoes.csv` | inserir, buscar e remover com a árvore completa: tempo, comparações e altura |

### 2.2 Interface gráfica (gráficos e operações)

```
javac -encoding UTF-8 -d out -sourcepath src src\gui\JanelaPrincipal.java
java -cp out gui.JanelaPrincipal
```

Rode o `Main` antes (item 2.1) para a aba "Gráficos" ter dados. A janela tem duas abas:

- **Operações:** carrega N registros do dataset (ou insere um tornado manualmente, ou o próximo do arquivo) e aplica **inserir, buscar e remover nas três árvores ao mesmo tempo**. A tabela mostra, para cada árvore, o resultado, as comparações, o tempo e a altura. Também mostra os percursos pré-ordem, em ordem e pós-ordem da árvore escolhida.
- **Gráficos:** lê os CSVs de `results/` e desenha os gráficos de comparação (ver item 3).

### 2.3 Testes automatizados

```
javac -encoding UTF-8 -d out -sourcepath src src\testes\TesteArvores.java
java -cp out testes.TesteArvores
```

Roda 40 testes (inserção, busca, remoção com rebalanceamento, percursos, altura, contador de comparações e integração com o dataset) e termina com `40 passaram, 0 falharam`. Os resultados das árvores são comparados com um `TreeMap` do Java.

## 3. Como gerar os gráficos

Os gráficos são gerados pela própria interface, na aba **Gráficos**, a partir dos CSVs de `results/`:

1. Rode o `Main` (item 2.1) e abra a janela (item 2.2).
2. Escolha o gráfico na lista **Gráfico**. Há 7 opções:
   - *Altura × elementos inseridos* (visualização do balanceamento);
   - *Comparações acumuladas × elementos*;
   - *Tempo acumulado de inserção × elementos*;
   - *Tempo médio por inserção × elementos*;
   - *Comparações médias por operação* (inserir, buscar, remover);
   - *Tempo médio por operação*;
   - *Altura após cada operação*.
3. A **escala logarítmica** liga sozinha quando uma árvore tem valores muito maiores que as outras (a BST); dá para desmarcar.
4. Passe o mouse sobre as linhas para ver os valores. O botão **Atualizar** relê os CSVs depois de rodar o `Main` de novo.
5. **Salvar PNG** grava o gráfico atual como imagem.

## 4. Estrutura do projeto

```
data/
  tornado_full.csv        dataset completo (74.956 registros)
  tornado_sample.csv      amostra pequena (517 registros), só para testes rápidos
results/                  CSVs gerados pelo Main
src/
  Main.java               roda os benchmarks e exporta os CSVs
  tree/                   Tree (interface), BSTree, AVLTree, RedBlackTree
  model/Tornado.java      registro de tornado (o dado guardado em cada nó)
  io/                     TornadoCsvLoader (lê o CSV) e ResultExporter (grava os CSVs)
  metrics/Benchmark.java  medições de altura, comparações e tempo
  gui/                    interface Swing (JanelaPrincipal, PainelOperacoes, PainelGraficos, ...)
  testes/TesteArvores.java testes automatizados
```

## 5. Métodos implementados

As três árvores implementam a interface `Tree<K, V>`, então o resto do programa trata todas do mesmo jeito.

| Método | O que faz |
|---|---|
| `inserir(chave, dado)` | insere (se a chave já existe, substitui o dado) |
| `buscar(chave)` | devolve o dado, ou `null` se não existir |
| `remover(chave)` | remove; na AVL e na Rubro-Negra rebalanceia a árvore |
| `altura()` | altura em arestas (árvore vazia = -1, uma folha = 0) |
| `percorrer(ordem)` | lista de chaves em `"pre"`, `"in"` ou `"pos"` |
| `contadorComparacoes()` / `resetarContador()` | instrumentação para as métricas |

## 6. Dataset

- **Fonte:** banco de dados de tornados do *Storm Prediction Center* (NOAA / National Weather Service), arquivos CSV públicos em <https://www.spc.noaa.gov/wcm/#data>. A descrição das colunas está em <https://www.spc.noaa.gov/wcm/data/SPC_severe_database_description.pdf>.
- **Conteúdo:** 74.956 registros de tornados nos EUA, de 1950 a 2025, com data, hora, estado, magnitude, feridos, mortos, prejuízo, coordenadas de início e fim, comprimento e largura.
- **Chave:** `aaaaMMddHHmm` (data e hora) × 1000, mais um número de desempate para tornados no mesmo minuto. É única, cronológica e funciona em todos os anos. O campo `om` do arquivo não serve como chave porque, a partir de 2007, deixa de ser numérico (ex.: `0710162316-01`).
- **Dado:** o objeto `Tornado` com os demais campos.
- Observações: a magnitude `-9` significa "não informada", e o campo de prejuízo (`loss`) muda de escala ao longo dos anos (veja a especificação do SPC); ele não é usado como chave.
- Os dados entram nas árvores **na ordem do arquivo (cronológica)**, ou seja, com chaves quase crescentes. Esse é o pior caso para uma árvore sem balanceamento.

## 7. Formato dos resultados

- Os CSVs usam **ponto como separador decimal** (`144.100` significa 144,1) e vírgula entre as colunas.
- **Tempos em microssegundos (µs)**, com 3 casas decimais (0,001 µs = 1 ns). Cada tempo é a **mediana de 5 repetições**, depois de uma passada de aquecimento da JVM, para reduzir o ruído de compilação e coleta de lixo.
- Os gráficos mostram o tempo acumulado em ms e os demais tempos em µs.
- As comparações são exatas e não dependem da máquina; os tempos variam um pouco a cada execução.

### Valores de referência (74.956 registros, ordem cronológica)

| | BST | AVL | Rubro-Negra |
|---|---|---|---|
| Altura final | 6.921 | 17 | 28 |
| Comparações para inserir todos | 223.971.432 | 1.218.344 | 1.907.091 |
| Comparações médias por busca | 2.989,04 | 15,50 | 16,12 |
| Comparações médias por remoção | 2.245,40 | 16,01 | 15,10 |
| Altura após remover 50% das chaves | 3.524 | 17 | 19 |

Com os dados em ordem crescente, a BST vira praticamente uma lista encadeada, e as árvores balanceadas mantêm a altura próxima de log₂(n).

## 8. Referências

- NOAA / NWS Storm Prediction Center. *Severe Weather Database Files*. <https://www.spc.noaa.gov/wcm/#data>
- ZIVIANI, Nivio; BOTELHO, Fabiano C. *Projeto de algoritmos: com implementações em Java e C++*. São Paulo: Cengage Learning, 2007.
- CORMEN, Thomas H. et al. *Algoritmos: teoria e prática*. 3. ed. Rio de Janeiro: Elsevier, 2012.
- GOODRICH, Michael T.; TAMASSIA, Roberto. *Estruturas de dados & algoritmos em Java*. Bookman, 2013.