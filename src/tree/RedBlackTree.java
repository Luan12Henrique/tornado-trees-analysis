package tree; 

import java.util.List;
import java.util.ArrayList;

public class RedBlackTree<K extends Comparable<K>, V> implements Tree<K, V> {
     private static final boolean VERMELHO = true;
    private static final boolean PRETO = false;

    private NoRB raiz;
    private int comparacoes;

    private class NoRB {
        K chave;
        V valor;
        NoRB esquerda;
        NoRB direita;
        NoRB pai;
        boolean cor;

        NoRB(K chave, V valor) {
            this.chave = chave;
            this.valor = valor;
            this.esquerda = null;
            this.direita = null;
            this.pai = null;
            this.cor = VERMELHO;
        }
    }

    public RedBlackTree() {
        raiz = null;
        comparacoes = 0;
    }

    //inserção 
    @Override
    public void inserir(K chave, V valor) {

        NoRB novo = new NoRB(chave, valor);

        if (raiz == null) {
            novo.cor = PRETO;
            raiz = novo;
            return;
        }

        NoRB atual = raiz;
        NoRB pai = null;

        while (atual != null) {
            pai = atual;

            comparacoes++;
            int cmp = chave.compareTo(atual.chave);

            if (cmp < 0) {
                atual = atual.esquerda;
            } else if (cmp > 0) {
                atual = atual.direita;
            } else {
                atual.valor = valor;
                return;
            }
        }

        novo.pai = pai;

        if (chave.compareTo(pai.chave) < 0) {
            pai.esquerda = novo;
        } else {
            pai.direita = novo;
        }

        corrigirInsercao(novo);
    }

    private void corrigirInsercao(NoRB no) {

        while (no != raiz && cor(no.pai) == VERMELHO) {

            NoRB pai = no.pai;
            NoRB avo = pai.pai;

            if (pai == avo.esquerda) {

                NoRB tio = avo.direita;

                if (cor(tio) == VERMELHO) {

                    pai.cor = PRETO;
                    tio.cor = PRETO;
                    avo.cor = VERMELHO;

                    no = avo;

                } else {

                    if (no == pai.direita) {
                        no = pai;
                        rotacaoEsquerda(no);
                        pai = no.pai;
                        avo = pai.pai;
                    }
                    pai.cor = PRETO;
                    avo.cor = VERMELHO;

                    rotacaoDireita(avo);
                }

            } else {

                NoRB tio = avo.esquerda;

                if (cor(tio) == VERMELHO) {

                    pai.cor = PRETO;
                    tio.cor = PRETO;
                    avo.cor = VERMELHO;

                    no = avo;
                } else {
                    if (no == pai.esquerda) {
                        no = pai;
                        rotacaoDireita(no);
                        pai = no.pai;
                        avo = pai.pai;
                    }
                    pai.cor = PRETO;
                    avo.cor = VERMELHO;
                    
                    rotacaoEsquerda(avo);
                }
            }
        }

        raiz.cor = PRETO;
    }

   //busca
    @Override
    public V buscar(K chave) {

        NoRB atual = raiz;

        while (atual != null) {

            comparacoes++;

            int cmp = chave.compareTo(atual.chave);

            if (cmp == 0) {
                return atual.valor;
            }

            if (cmp < 0) {
                atual = atual.esquerda;
            } else {
                atual = atual.direita;
            }
        }

        return null;
    }

    //remocão
    @Override
    public void remover(K chave) {

        NoRB no = buscarNo(chave);

        if (no == null) {
            return;
        }

        removerNo(no);
    }

    private NoRB buscarNo(K chave) {

        NoRB atual = raiz;

        while (atual != null) {

            comparacoes++;

            int cmp = chave.compareTo(atual.chave);

            if (cmp == 0) {
                return atual;
            }

            if (cmp < 0) {
                atual = atual.esquerda;
            } else {
                atual = atual.direita;
            }
        }

        return null;
    }

    private void removerNo(NoRB z) {

        NoRB y = z;
        boolean corOriginal = y.cor;

        NoRB x;
        NoRB paiX;

        if (z.esquerda == null) {

            x = z.direita;
            paiX = z.pai;

            transplantar(z, z.direita);

        } else if (z.direita == null) {

            x = z.esquerda;
            paiX = z.pai;

            transplantar(z, z.esquerda);

        } else {

            y = minimo(z.direita);
            corOriginal = y.cor;

            x = y.direita;

            if (y.pai == z) {

                paiX = y;

                if (x != null) {
                    x.pai = y;
                }

            }else {

                paiX = y.pai;

                transplantar(y, y.direita);

                y.direita = z.direita;

                if (y.direita != null) {
                    y.direita.pai = y;
                }
            }

            transplantar(z, y);

            y.esquerda = z.esquerda;

            if (y.esquerda != null) {
                y.esquerda.pai = y;
            }

            y.cor = z.cor;
        }

        if (corOriginal == PRETO) {
            corrigirRemocao(x, paiX);
        }
    }

    private void corrigirRemocao(NoRB x, NoRB paiX) {

        while (x != raiz && cor(x) == PRETO) {

            if (x == filhoEsquerdo(paiX)) {

                NoRB irmao = filhoDireito(paiX);

                if (cor(irmao) == VERMELHO) {

                    irmao.cor = PRETO;
                    paiX.cor = VERMELHO;

                    rotacaoEsquerda(paiX);

                    irmao = filhoDireito(paiX);
                }

                if(irmao == null) {
                    x = paiX;
                    paiX = x != null ? x.pai : null;
                    continue;
                }

                if (cor(filhoEsquerdo(irmao)) == PRETO &&
                    cor(filhoDireito(irmao)) == PRETO) {

                    irmao.cor = VERMELHO;

                    x = paiX;
                    paiX = x != null ? x.pai : null;

                } else {

                    if (cor(filhoDireito(irmao)) == PRETO) {

                        if (filhoEsquerdo(irmao) != null) {
                            filhoEsquerdo(irmao).cor = PRETO;
                        }

                        irmao.cor = VERMELHO;

                        rotacaoDireita(irmao);

                        irmao = filhoDireito(paiX);
                    }

                    irmao.cor = paiX.cor;
                    paiX.cor = PRETO;

                    if (filhoDireito(irmao) != null) {
                        filhoDireito(irmao).cor = PRETO;
                    }

                    rotacaoEsquerda(paiX);

                    x = raiz;
                    paiX = null;
                }

            } else{

                NoRB irmao = filhoEsquerdo(paiX);

                if (cor(irmao) == VERMELHO) {

                    irmao.cor = PRETO;
                    paiX.cor = VERMELHO;

                    rotacaoDireita(paiX);

                    irmao = filhoEsquerdo(paiX);
                }

                if(irmao == null) {
                    x = paiX;
                    paiX = x != null ? x.pai : null;
                    continue;
                }

                if(cor(filhoDireito(irmao)) == PRETO &&
                    cor(filhoEsquerdo(irmao)) == PRETO) {

                    irmao.cor = VERMELHO;

                    x = paiX;
                    paiX = x != null ? x.pai : null;

                } else {

                    if (cor(filhoEsquerdo(irmao)) == PRETO) {

                        if (filhoDireito(irmao) != null) {
                            filhoDireito(irmao).cor = PRETO;
                        }

                        irmao.cor = VERMELHO;

                        rotacaoEsquerda(irmao);

                        irmao = filhoEsquerdo(paiX);
                    }

                    irmao.cor = paiX.cor;
                    paiX.cor = PRETO;

                    if (filhoEsquerdo(irmao) != null) {
                        filhoEsquerdo(irmao).cor = PRETO;
                    }

                    rotacaoDireita(paiX);

                    x = raiz;
                    paiX = null;
                }
            }
        }

        if (x != null) {
            x.cor = PRETO;
        }
    }

    //altura
    @Override
    public int altura() {
        return altura(raiz);
    }

    private int altura(NoRB no) {

        if (no == null) {
            return -1; //mesma coisa que arvore avl e abb
        }

        return Math.max(
            altura(no.esquerda),
            altura(no.direita)
        ) + 1;
    }

    //percorrer
    @Override
    public List<K> percorrer(String ordem) {

        List<K> resultado = new ArrayList<>();

        percorrerRecursivo(raiz, ordem, resultado);

        return resultado;
    }

    private void percorrerRecursivo(
            NoRB atual,
            String ordem,
            List<K> resultado) {

        if (atual == null) {
            return;
        }

        if (ordem.equals("pre")) {
            resultado.add(atual.chave);
        }

        percorrerRecursivo(
            atual.esquerda,
            ordem,
            resultado
        );

        if (ordem.equals("in")) {
            resultado.add(atual.chave);
        }

        percorrerRecursivo(
            atual.direita,
            ordem,
            resultado
        );

        if (ordem.equals("pos")) {
            resultado.add(atual.chave);
        }
    }

    //comparacoes
    @Override
    public int contadorComparacoes() {
        return comparacoes;
    }

    @Override
    public void resetarContador() {
        comparacoes = 0;
    }

    //rotacao esquerda
    private void rotacaoEsquerda(NoRB x) {

        NoRB y = x.direita;

        x.direita = y.esquerda;

        if (y.esquerda != null) {
            y.esquerda.pai = x;
        }

        y.pai = x.pai;

        if (x.pai == null) {
            raiz = y;

        } else if (x == x.pai.esquerda) {
            x.pai.esquerda = y;

        } else {
            x.pai.direita = y;
        }

        y.esquerda = x;
        x.pai = y;
    }

    //rotacao direita
    private void rotacaoDireita(NoRB x) {

        NoRB y = x.esquerda;

        x.esquerda = y.direita;

        if (y.direita != null) {
            y.direita.pai = x;
        }

        y.pai = x.pai;

        if (x.pai == null) {
            raiz = y;

        } else if (x == x.pai.direita) {
            x.pai.direita = y;

        } else {
            x.pai.esquerda = y;
        }

        y.direita = x;
        x.pai = y;
    }

    //metodo aux
    private boolean cor(NoRB no) {

        if (no == null) {
            return PRETO;
        }

        return no.cor;
    }

    private NoRB filhoEsquerdo(NoRB no) {

        if (no == null) {
            return null;
        }

        return no.esquerda;
    }

    private NoRB filhoDireito(NoRB no) {

        if (no == null) {
            return null;
        }

        return no.direita;
    }

    private NoRB minimo(NoRB no) {

        NoRB atual = no;

        while (atual.esquerda != null) {
            atual = atual.esquerda;
        }

        return atual;
    }

    private void transplantar(NoRB u, NoRB v) {

        if (u.pai == null) {

            raiz = v;

        } else if (u == u.pai.esquerda) {

            u.pai.esquerda = v;

        } else {

            u.pai.direita = v;
        }

        if (v != null) {
            v.pai = u.pai;
        }
    }
    }