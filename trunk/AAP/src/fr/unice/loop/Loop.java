package fr.unice.aap.loop;

public class Loop {

	private String nom;
	private int debutLoop;
	private int finLoop;
	private String titreChanson;
	
	public Loop(String nom, int debutLoop, int finLoop, String titreChanson) {
		super();
		this.nom = nom;
		this.debutLoop = debutLoop;
		this.finLoop = finLoop;
		this.titreChanson = titreChanson;
	}
	
	public Loop() {
		
	}

	public String getTitreChanson() {
		return titreChanson;
	}

	public void setTitreChanson(String titreChanson) {
		this.titreChanson = titreChanson;
	}

	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public int getDebutLoop() {
		return debutLoop;
	}
	public void setDebutLoop(int debutLoop) {
		this.debutLoop = debutLoop;
	}
	public int getFinLoop() {
		return finLoop;
	}
	public void setFinLoop(int finLoop) {
		this.finLoop = finLoop;
	}
	@Override
	public String toString() {
		return "Loop [nom=" + nom + ", debutLoop=" + debutLoop + ", finLoop="
				+ finLoop + "]";
	}
	
}
