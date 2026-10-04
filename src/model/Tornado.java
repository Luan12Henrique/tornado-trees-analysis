package model;

/**
 * Representa um registro de tornado do dataset da NOAA/SPC.
 * Esta classe é o "dado" (V) guardado em cada nó das árvores,
 * associado a uma chave única (ano + número do tornado).
 */
public class Tornado {

    private final long id;
    private final int ano;
    private final int mes;
    private final int dia;
    private final String estado;
    private final int magnitude;
    private final int feridos;
    private final int mortos;
    private final double prejuizo;
    private final double latitudeInicio;
    private final double longitudeInicio;
    private final double latitudeFim;
    private final double longitudeFim;
    private final double comprimento;
    private final double largura;

    public Tornado(long id, int ano, int mes, int dia, String estado, int magnitude,
            int feridos, int mortos, double prejuizo,
            double latitudeInicio, double longitudeInicio,
            double latitudeFim, double longitudeFim,
            double comprimento, double largura) {
        this.id = id;
        this.ano = ano;
        this.mes = mes;
        this.dia = dia;
        this.estado = estado;
        this.magnitude = magnitude;
        this.feridos = feridos;
        this.mortos = mortos;
        this.prejuizo = prejuizo;
        this.latitudeInicio = latitudeInicio;
        this.longitudeInicio = longitudeInicio;
        this.latitudeFim = latitudeFim;
        this.longitudeFim = longitudeFim;
        this.comprimento = comprimento;
        this.largura = largura;
    }

    public long getId() {
        return id;
    }

    public int getAno() {
        return ano;
    }

    public int getMes() {
        return mes;
    }

    public int getDia() {
        return dia;
    }

    public String getEstado() {
        return estado;
    }

    public int getMagnitude() {
        return magnitude;
    }

    public int getFeridos() {
        return feridos;
    }

    public int getMortos() {
        return mortos;
    }

    public double getPrejuizo() {
        return prejuizo;
    }

    public double getLatitudeInicio() {
        return latitudeInicio;
    }

    public double getLongitudeInicio() {
        return longitudeInicio;
    }

    public double getLatitudeFim() {
        return latitudeFim;
    }

    public double getLongitudeFim() {
        return longitudeFim;
    }

    public double getComprimento() {
        return comprimento;
    }

    public double getLargura() {
        return largura;
    }

    @Override
    public String toString() {
        return "Tornado{id=" + id + ", ano=" + ano + ", estado='" + estado +
                "', magnitude=" + magnitude + ", mortos=" + mortos +
                ", feridos=" + feridos + "}";
    }
}