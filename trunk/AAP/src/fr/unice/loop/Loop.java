package fr.unice.loop;

/**
 * Classe Loop, correspond a une boucle
 * @author Mic
 *
 */
public class Loop {

	/**
	 * Nom de la boucle
	 */
	private String nom;
	/**
	 * Debut de la boucle en milliseconde
	 */
	private int debutLoop;
	/**
	 * Fin de la boucle en milliseconde
	 */
	private int finLoop;
	/**
	 * Titre de la chanson de la boucle
	 */
	private String titreChanson;
	
	/**
	 * Constructeur
	 * @param nom Nom de la boucle
	 * @param debutLoop Debut de la boucle en milliseconde
	 * @param finLoop Fin de la boucle en milliseconde
	 * @param titreChanson Titre de la chanson associe a la boucle
	 */
	public Loop(String nom, int debutLoop, int finLoop, String titreChanson) {
		super();
		this.nom = nom;
		this.debutLoop = debutLoop;
		this.finLoop = finLoop;
		this.titreChanson = titreChanson;
	}
	/**
	 * Constructeur vide
	 */
	public Loop() {
		
	}
    /**
     * Getter de  titreChanson
     * @return le titre de la chanson associe a la boucle
     */
	public String getTitreChanson() {
		return titreChanson;
	}

	/**
	 * Setter de titre chanson
	 * @param titreChanson titre de la chanson associe
	 */
	public void setTitreChanson(String titreChanson) {
		this.titreChanson = titreChanson;
	}

	/**
	 * Getter de nom
	 * @return le nom de la boucle
	 */
	public String getNom() {
		return nom;
	}
	/**
	 * Setter de nom
	 * @param nom Nom de la boucle
	 */
	public void setNom(String nom) {
		this.nom = nom;
	}
	/**
	 * Getter de debutLoop
	 * @return le debut de la boucle en milliseconde
	 */
	public int getDebutLoop() {
		return debutLoop;
	}
	/**
	 * Setter de debutLoop
	 * @param debutLoop debut de la boucle en milliseconde
	 */
	public void setDebutLoop(int debutLoop) {
		this.debutLoop = debutLoop;
	}
	/**
	 * Getter de finLoop
	 * @return la fin de la boucle en milliseconde
	 */
	public int getFinLoop() {
		return finLoop;
	}
	/**
	 * Setter de finLoop
	 * @param finLoop la fin de la boucle en milliseconde
	 */
	public void setFinLoop(int finLoop) {
		this.finLoop = finLoop;
	}
	/**
	 * Methode ToString
	 */
	@Override
	public String toString() {
		return "Loop [nom=" + nom + ", debutLoop=" + debutLoop + ", finLoop="
				+ finLoop + "]";
	}
	
}
