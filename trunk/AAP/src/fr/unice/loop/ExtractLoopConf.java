package fr.unice.loop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.os.Environment;
import android.util.Log;

/**
 * <p>
 * Classe technique permettant d'accéder au fichier XML de sauvegarde
 * </p>
 * @author Mic
 *
 */
public class ExtractLoopConf {

	/**
	 * Constructeur
	 */
	public ExtractLoopConf(){};
	
	/**
	 * Methode permettant d'ajouter (sauvgarder) une boucle dans le fichier XML
	 * @param loop Boucle a ajouter
	 */
	public static void addLoop(Loop loop)
	{
		try
		{
			DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
            fabrique.setIgnoringElementContentWhitespace(true);
            DocumentBuilder constructeur = fabrique.newDocumentBuilder();
            //début parsage
            File xml = new File("/sdcard/loopConfig.xml");
            Document document = constructeur.parse(xml);
            NodeList nL = document.getElementsByTagName("root");
            Node n = nL.item(0);
            Node chansonNode = document.createElement("chanson");
            
            //Test pour savoir si le noeud chanson associé existe deja
            ArrayList<Loop> lstLoopExiste = getLoops(loop.getTitreChanson());
            if(lstLoopExiste.size()!=0)
            {
            	NodeList lstChansonNode = n.getChildNodes();
            	for(int i=0;i<lstChansonNode.getLength();i++)
            	{
            		Node nChanson = lstChansonNode.item(i);
            		if(nChanson.getNodeType() == Node.ELEMENT_NODE)
            		{
	            		if(nChanson.getAttributes().getNamedItem("titre").getTextContent().equals(loop.getTitreChanson()))
	            		{
	            			chansonNode = nChanson;
	            		}
            		}
            	}
            }
            else
            {
	            //Noeud chanson
	            Element elChanson = (Element)chansonNode;
	            elChanson.setAttribute("titre", loop.getTitreChanson());
	            n.appendChild(chansonNode);
            }
            //Noeud Loop
            Node loopNode = document.createElement("loop");
            chansonNode.appendChild(loopNode);
            // Noeud nom
            Node nomNode = document.createElement("nom");
            loopNode.appendChild(nomNode);
            nomNode.setTextContent(loop.getNom());
            //Noeud debut
            Node debutNode = document.createElement("debut");
            debutNode.setTextContent(String.valueOf(loop.getDebutLoop()));
            loopNode.appendChild(debutNode);
            //Noeud fin
            Node finNode = document.createElement("fin");
            finNode.setTextContent(String.valueOf(loop.getFinLoop()));
            loopNode.appendChild(finNode);
            
            //saving
            TransformerFactory fabrique2 = TransformerFactory.newInstance();
            Transformer transformer = fabrique2.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            Source source = new DOMSource(document);
            Result resultat = new StreamResult(xml);
            transformer.transform(source,resultat);
		}
		catch(Exception e)
		{
			Log.i("INFO", "Error ecriture XML");
			e.printStackTrace();
		}
	}
	
	/**
	 * Methode permettant de récupérer toutes les boucles d'une chanson 
	 * @param titre Titre de la chanson
	 * @return retourne toutes les boucles de la chanson "titre"
	 */
	public static ArrayList<Loop> getLoops(String titre)
	{
		ArrayList<Loop> lstLoop = new ArrayList<Loop>();
		try
        {
            DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
            fabrique.setIgnoringElementContentWhitespace(true);
            DocumentBuilder constructeur = fabrique.newDocumentBuilder();
            
            File xml = new File("/sdcard/loopConfig.xml");
            Document document = constructeur.parse(xml);
            NodeList list = document.getElementsByTagName("chanson");
            for(int i =0; i<list.getLength(); i++)
            {
                Node noeud = list.item(i);
                if(noeud.getAttributes().getNamedItem("titre").getTextContent().equals(titre))
                {
	                if(noeud.getNodeType() == Node.ELEMENT_NODE)
	                {
		                NodeList sousList = noeud.getChildNodes();
		                String nom= "";
		                int debutL= 0;
		                int finL = 0;
		                for(int j=0;j<sousList.getLength();j++)
		                {
		                    Node n = sousList.item(j);
		                    if(n.getNodeType() == Node.ELEMENT_NODE)
		                    {
		                    	NodeList lst = n.getChildNodes();
		                    	for(int k=0;k<lst.getLength();k++)
		                    	{
		                    		Node nd = lst.item(k);
		                    		 if(nd.getNodeType() == Node.ELEMENT_NODE)
		                             {
			                    		if(nd.getNodeName().equals("nom"))
				                        {
				                            nom = nd.getTextContent();
				                        }
				                        if(nd.getNodeName().equals("debut"))
				                        {
				                            debutL = Integer.parseInt(nd.getTextContent());
				                        }
				                        if(nd.getNodeName().equals("fin"))
				                        {
				                            finL = Integer.parseInt(nd.getTextContent());
				                        }
		                             }
		                    	}
		                    	lstLoop.add(new Loop(nom,debutL,finL,titre));
		                    } 
		                }
                }
                }
            }
            return lstLoop;
        }
        catch(Exception e)
        {
            System.out.println("ERROR => Parser XML");
            e.printStackTrace();
            return lstLoop;
        }
        
	}
	
	/**
	 * Methode permettant de récupérer une boucle 
	 * @param nomLoop Nom de la boucle recherché
	 * @param titre Nom de la chanson de la boucle recherché
	 * @return retourne la boucle appartenant a la chanson "titre" et ayant pour nom "nomLoop"
	 */
	public static Loop getLoop(String nomLoop, String titre)
	{
		Loop l = new Loop();
		ArrayList<Loop> lstLoop = getLoops(titre);
		for(int i=0;i<lstLoop.size();i++)
		{
			if(lstLoop.get(i).getNom().equals(nomLoop))
			{
				l = lstLoop.get(i);
				return l;
			}
		}
		return null;
	}
	
	/**
	 * Methode verifiant l'existance du fichier XML de sauvegarde, a la racine de la carte SD.
	 * Si le fichier n'existe pas, il est cree.
	 */
	public static void verifXML()
	{
		File fichiers[] = Environment.getExternalStorageDirectory().listFiles();
        Boolean xmlExist = false;
        for(int i=0;i<fichiers.length;i++)
        {
        	if(fichiers[i].isFile())
        	{
        		if(fichiers[i].getName().equals("loopConfig.xml"))
        		{
        			xmlExist = true;
        		}
        	}
        }
        if(!xmlExist)
        {
			DocumentBuilder builder;
			try {
				builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			
	        Document doc = builder.newDocument();
	        Element root = doc.createElement("root");
	        doc.appendChild(root);
	        
	        TransformerFactory xformFactory = TransformerFactory.newInstance();
	       Transformer idTransform = xformFactory.newTransformer();
	       Source input = new DOMSource(doc);
	       Result output = new StreamResult(new File("/sdcard/loopConfig.xml"));
	       idTransform.transform(input, output);
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	
}
