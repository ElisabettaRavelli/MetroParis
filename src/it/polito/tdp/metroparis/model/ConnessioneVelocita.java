package it.polito.tdp.metroparis.model;

public class ConnessioneVelocita {
	
	private int stazP;
	private int stazA;
	private double velocita;
	public ConnessioneVelocita(int stazP, int stazA, double velocita) {
		super();
		this.stazP = stazP;
		this.stazA = stazA;
		this.velocita = velocita;
	}
	public int getStazP() {
		return stazP;
	}
	public void setStazP(int stazP) {
		this.stazP = stazP;
	}
	public int getStazA() {
		return stazA;
	}
	public void setStazA(int stazA) {
		this.stazA = stazA;
	}
	public double getVelocita() {
		return velocita;
	}
	public void setVelocita(double velocita) {
		this.velocita = velocita;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + stazP;
		result = prime * result + stazA;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConnessioneVelocita other = (ConnessioneVelocita) obj;
		if (stazP != other.stazP)
			return false;
		if (stazA != other.stazA)
			return false;
		return true;
	}
	

}
