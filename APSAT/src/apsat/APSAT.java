package apsat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author FECHINO Cedric
 */

public class APSAT{
	
	public String address;
	public String problem;
	public String nature;
	public int nbClauses;
	public int nbVariables;
	public int nbAffectations = 1;
	public ArrayList<ArrayList<Integer>> P = new ArrayList<ArrayList<Integer>>();
	public ArrayList<boolean[]> M = new ArrayList<boolean[]>();
	public ArrayList<boolean[]> K = new ArrayList<boolean[]>();
	public double timer;
	
	public static void main(String[] args) {
		StringBuffer address = new StringBuffer(args[0]);
		for(int i = 1; i < args.length; i++){ address.append(" "+args[i]); }
		new APSAT(address.toString());
	}
	
	public APSAT(String adress) {
		this.address = adress;
		System.out.println("c Traitement des données...");
		this.readFile(address);
	}
	
	public void readFile(String address){
		try { 
			timer = System.nanoTime();
			File file = new File(address);    
		    FileReader fReader = new FileReader(file);
		    BufferedReader bReader = new BufferedReader(fReader);  
		    String line;
		    Pattern pattern = Pattern.compile("[^\\s]+");
		    while((line = bReader.readLine()) != null){
		    	Matcher matcher = pattern.matcher(line);
	            matcher.find();
	            if(!matcher.group().equals("c")) {
	            	if(matcher.group().equals("p")){
		            	problem = matcher.group();
		            	matcher.find();
		            	nature = matcher.group();
		            	matcher.find();
		            	nbClauses = Integer.valueOf(matcher.group());
		            	matcher.find();
		            	nbVariables = Integer.valueOf(matcher.group());
		            }else{
		            	P.add(new ArrayList<Integer>()); 
		            	do{
		            		int i = Integer.valueOf(matcher.group());
		            		P.get(P.size()-1).add(i);
		            	}while(matcher.find()); 
		            }
	            }
		    }
		    fReader.close();
		    bReader.close();
		    this.rangerP();
		    this.PtoM();
		    this.trouverAffectationsValidePourM();
		    this.afficherAffectationsValides(); 
		    timer = (System.nanoTime() - timer) / 1000000000;
		    System.out.println("c Temps écoulé pour traitement des données: "+timer+"s");
		} catch (Exception e) { e.printStackTrace(); }     
	}

	public void rangerP() {
		
		Comparator<ArrayList<Integer>> sizeComparator = new Comparator<ArrayList<Integer>>() {
			@Override
			public int compare(ArrayList<Integer> a, ArrayList<Integer> b) {
				 return Integer.compare(a.size(), b.size());
			}
	    };

	    Collections.sort(P, sizeComparator);
	}
	
	public void PtoM() {
		for(ArrayList<Integer> p : P){
			M.add(new boolean[nbVariables*2]); 
			for(int i : p){
				if(i > 0){
					M.get(M.size()-1)[Math.abs(i)*2-2] = false;
					M.get(M.size()-1)[Math.abs(i)*2-1] = true;
				}else if(i < 0){
					M.get(M.size()-1)[Math.abs(i)*2-2] = true;
					M.get(M.size()-1)[Math.abs(i)*2-1] = true;
				}
			}
		}
	}
	
	public void trouverAffectationsValidePourM() {
		boolean[] k = new boolean[this.nbVariables*2];
		this.supprimerMdeK(k, 0);
	}

	private void supprimerMdeK(boolean[] k, int m) {
		if(K.size() < this.nbAffectations) {
			if(m < M.size()) {
				if(this.verifierEgaliteVirtuel(k, M.get(m))) {
					for(int i = 0; i < this.nbVariables*2; i+=2) {
						if(!k[i+1] && M.get(m)[i+1] ) {
							k[i+1] = true;
							k[i] = !M.get(m)[i];
							this.supprimerMdeK(k.clone(), m+1);
							k[i] = !k[i];
						}
					}
				}else {
					this.supprimerMdeK(k, m+1);
				}
			}else {
				K.add(k);
			}
		}
	}
	
	private boolean verifierEgaliteVirtuel(boolean[] k, boolean[] m) {
		for(int l = 0; l < this.nbVariables*2; l+=2) {
			if( k[l] != m[l] && k[l+1] && m[l+1] ) {
				return false;
			}
		}
		return true;
	}
	
	public void afficherAffectationsValides() {
		StringBuffer buffer = new StringBuffer();
		if(K.size() > 0) {
			System.out.println("s SATISFIABLE");
			for(boolean[] k : K){
				System.out.print("v ");
				for(int l = 0; l < this.nbVariables*2; l+=2) {
					if(k[l+1]) {
						if(k[l]){
							buffer.append((l/2+1) + " ");
						}else {
							buffer.append(-(l/2+1) + " ");
						}
					}
				}
				buffer.append("0");
			}
			System.out.println(buffer.toString());
		}else {
			System.out.println("s UNSATISFIABLE");
		}
		
	}
}
