import java.io.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Conta implements ITaxas, Serializable {

    private int numero;
    private int numAgencia;
    private Cliente dono;
    protected double saldo;
    protected double limiteMin, limiteMax;
    private List<Operacao> operacoes;
    private int proximaOperacao;
    private static int totalContas = 0;
    @Serial
    private static final long serialVersionUID = 1L;

    public Conta(int numero, int numAgencia, Cliente dono, double saldo) {
        this.numero = numero;
        this.numAgencia = numAgencia;
        this.dono = dono;
        this.saldo = saldo;

        this.operacoes = new ArrayList<>();
        this.proximaOperacao = 0;

        totalContas++;
    }

    public void depositar(double valor) throws ValorNegativoException {

        if (valor < 0) {
            throw new ValorNegativoException("Depósito com valor negativo: " + valor);
        } else {
            this.saldo += valor;
            this.operacoes.add(new OperacaoDeposito(valor));
        }
    }

    public boolean sacar(double valor) throws ValorNegativoException, SemLimiteException {

        if (valor < 0) {
            throw new ValorNegativoException("Saque com valor negativo: " + valor);
        }

        if (valor < limiteMin) {
            throw new SemLimiteException("Valor de saque abaixo do limite: " + valor);
        }
        if (valor > limiteMax) {
            throw new SemLimiteException("Valor de saque acima do limite: " + valor);
        }

        if (valor <= this.saldo) {
            this.saldo -= valor;

            //Adiciona uma nova operacao de Saque
            this.operacoes.add(new OperacaoSaque(valor));
            return true;
        } else {
            return false;
        }
    }

    public boolean transferir(Conta destino, double valor) throws ValorNegativoException, SemLimiteException {

        boolean valorSacado = this.sacar(valor);

        if (valorSacado) {
            destino.depositar(valor);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {

        String ContaString = "\n" + "Conta " + this.getNumero() + "\n" + "Numero da agencia: " + this.getNumAgencia() + "\n"
                + "Dono: " + this.getDono() + "\n" + "Saldo: " + this.getSaldo()
                + "\n" + "Limite Mínimo: " + this.getLimiteMin() + "\n" + "Limite Máximo: " + getLimiteMax();

        return ContaString;
    }

    @Override
    public boolean equals(Object obj) {
        if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            Conta conta = (Conta) obj;
            if (this.numero == (conta.numero)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void imprimirExtrato(int ordem) {
        if (ordem == -1) {
            System.out.println("\n======= Extrato Conta " + this.numero + "na ordem b: ======================");
            Collections.sort(this.operacoes);
            for (Operacao atual : this.operacoes) {
                atual.imprimir();
            }

        } else if (ordem == 0) {
            System.out.println("\n======= Extrato Conta " + this.numero + "na ordem a: ======================");
            for (Operacao atual : this.operacoes) {
                atual.imprimir();
            }
        }
    }

    public void imprimirExtratoTaxas() {
        double totalTaxas = this.calculaTaxas();
        System.out.println("*=*=*=*=* Extrato de taxas " + dono.getNome() + " *=*=*=*=*");
        System.out.println("Manutenção da conta: " + this.calculaTaxas());

        System.out.println("Operações");
        for (Operacao operacao : this.operacoes) {
            if (operacao.getTipo() == 's') {
                //ou: if(operacao instanceof OperacaoSaque){}
                totalTaxas += operacao.calculaTaxas();
                System.out.println("Saque: " + operacao.calculaTaxas());

            }
        }
        System.out.printf("Total: %.2f\n", totalTaxas);
    }

    public void salvaDados(int numAgencia, int numConta) throws IOException, ClassNotFoundException {

        /*System.out.println("*-*-Antes do processo de serialização-*-*");
        System.out.println("Conta " + this.getNumero());
        System.out.println(this);
        System.out.println();
        System.out.println("-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
        */

        try {
            FileOutputStream arquivo = new FileOutputStream(numAgencia + "-" + numConta + ".ser");
            ObjectOutputStream serializa = new ObjectOutputStream(arquivo);
            serializa.writeObject(this);
            serializa.close();
            arquivo.close();
            System.out.println("\n-*-*Serialização*-*-");
            System.out.println(this);
        } catch (IOException exception) {
            System.out.println("Falha ao serializar a conta: " + exception.getMessage());
        }
    }

    public void carregaDados(int numAgencia, int numConta) {
        try {
            FileInputStream arquivo = new FileInputStream(numAgencia + "-" + numConta + ".ser");
            ObjectInputStream desserializa = new ObjectInputStream(arquivo);
            Conta desserializado = (Conta) desserializa.readObject();
            desserializa.close();
            arquivo.close();
            System.out.println("\n*-*-Desserialização-*-*");
            System.out.println(desserializado);
        } catch (IOException | ClassNotFoundException exception) {
            System.out.println("Falha ao desserializar a conta: " + getNumero());
            System.out.println(exception.getMessage());
        }
    }
    public int getNumero() {
        return numero;
    }

    public int getNumAgencia(){
        return numAgencia;
    }

    public Cliente getDono() {
        return dono;
    }

    public double getSaldo() {
        return saldo;
    }

    public double getLimiteMin() {
        return limiteMin;
    }

    public double getLimiteMax(){
        return limiteMax;
    }

    public static int getTotalContas() {
        return Conta.totalContas;
    }

    public abstract void setLimite(double limiteMin, double limiteMax) throws SemLimiteException, IllegalArgumentException;
}
