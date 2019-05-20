package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LatLngConfig;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.metroparis.db.MetroDAO;

/*
 * IMPORTANTE 
 * Per poter calcolare i cammini minimi ho bisogno di aggiungere i pesi sul grafo e quindi 
 * devo mettere come tipo 'DefaultWeightedEdge' al posto di 'DefaultEdge' per tutti i metodi.
 * Se si vuole tornare indietro basta rimettere il tipo per un grafo semplice non pesato.
 */

//CLASSE CHE GESTISCE IL GRAFO
public class Model {
	
	//DEFINISCO LA CLASSE INTERNAMENTE AL MODEL PERCHE' E' A LUI CHE SERVE
	private class EdgetreaversegraphListener implements TraversalListener<Fermata,DefaultWeightedEdge> {

		@Override
		public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {
		}

		@Override
		public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {
		}

		@Override
		public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> ev) {
			
			Fermata sourceVertex = grafo.getEdgeSource(ev.getEdge());
			Fermata targetVertex = grafo.getEdgeTarget(ev.getEdge());
			
			//se il figlio (target) non è ancora nella mappa ma il padre (source) è nella mappa
			if(!backVisit.containsKey(targetVertex)&& backVisit.containsKey(sourceVertex)) {
				backVisit.put(targetVertex, sourceVertex); //aggiungo alla mappa l'informazione
				//caso del grafo non orientato il padre viene dopo il figlio
			} else if(!backVisit.containsKey(sourceVertex)&& backVisit.containsKey(targetVertex)) {
				backVisit.put(sourceVertex, targetVertex);
			}
			
			
		}

		@Override
		public void vertexFinished(VertexTraversalEvent<Fermata> arg0) {
		}

		@Override
		public void vertexTraversed(VertexTraversalEvent<Fermata> arg0) {
		}
		
	}
	
//	private Graph<Fermata, DefaultEdge> grafo;
	private Graph<Fermata, DefaultWeightedEdge> grafo;
	private List<Fermata> fermate;
	private Map<Integer, Fermata> fermateIdMap;
	private Map<Fermata,Fermata> backVisit;
	
	public void creaGrafo() {
		
		//creo l'oggetto grafo
//		this.grafo = new SimpleDirectedGraph<>(DefaultEdge.class);
		
		//creo l'oggetto grafo pesato per il calcolo dei cammini minimi
		this.grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		
		//aggiungo i vertici
		MetroDAO dao = new MetroDAO();
		this.fermate = dao.getAllFermate();
		
		
		//crea idMAp
		this.fermateIdMap = new HashMap<>();
		for(Fermata f: this.fermate)
			fermateIdMap.put(f.getIdFermata(),f);
		
		Graphs.addAllVertices(this.grafo, this.fermate);
		
		/*
		//aggiungo gli archi (opzione 1)
		for(Fermata partenza: this.grafo.vertexSet()) {
			for(Fermata arrivo: this.grafo.vertexSet()){
				
				if(dao.esisteConnessione(partenza,arrivo)) {
					this.grafo.addEdge(partenza, arrivo);
				}
			}
		} 
		*/
		
		//aggiungo gli archi (opzione 2)
		for(Fermata partenza: this.grafo.vertexSet()) {
			List<Fermata> arrivi = dao.stazioniArrivo(partenza, fermateIdMap);
			
			for(Fermata arrivo: arrivi)
				this.grafo.addEdge(partenza, arrivo);
		}
		
		//aggiungo gli archi (opzione 3)
		
		//aggiungo i pesi agli archi
		List<ConnessioneVelocita> archipesati= dao.getConnessionieVelocita();		
		for(ConnessioneVelocita cp: archipesati) {
			Fermata partenza = fermateIdMap.get(cp.getStazP());
			Fermata arrivo = fermateIdMap.get(cp.getStazA());
			double distanza=LatLngTool.distance(partenza.getCoords(), arrivo.getCoords(), LengthUnit.KILOMETER);
			double peso = distanza/cp.getVelocita()*3600; //metto il tempo in secondi moltiplicando per 3600
			
			grafo.setEdgeWeight(partenza, arrivo, peso);
			//oppure si aggiungono archi e vertici insieme
			//Graphs.addEdgeWithVertices(grafo, arrivo, peso);
		
		}
		
	}
	

	public List<Fermata> fermateRaggiungibili(Fermata source){
		
		List<Fermata> result = new ArrayList<Fermata>();
		backVisit = new HashMap<>();

//      PER VISITARE UN GRAFO DEVO CREARE UN ITERATORE (per ogni visita devo creare un nuovo iteratore)
// 		Con visite diverse si ottengono sempre gli stessi oggetti ma in ordini differenti
		
//		per una visita in ampiezza uso questo oggetto iteratore
		GraphIterator<Fermata, DefaultWeightedEdge> it = new BreadthFirstIterator<>(this.grafo, source);
		
//		per una visita in profondità uso questo oggetto iteratore
//		GraphIterator<Fermata, DefaultEdge> it = new DepthFirstIterator<>(this.grafo, source);
		
//		Aggancio all'iteratore il listener
		//CLASSE ESTERNA PUBBLICA
//		it.addTraversalListener(new EdgetreaversegraphListener(backVisit, grafo));
		
		//CLASSE INTERNA PRIVATA AL MODEL
		it.addTraversalListener(new Model.EdgetreaversegraphListener());
		
		//CLASSE INTERNA ANONIMA AL MODEL	
/*		it.addTraversalListener(new TraversalListener<Fermata, DefaultEdge>() {

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> ev) {
				Fermata sourceVertex = grafo.getEdgeSource(ev.getEdge());
				Fermata targetVertex = grafo.getEdgeTarget(ev.getEdge());
				
				//se il figlio (target) non è ancora nella mappa ma il padre (source) è nella mappa
				if(!backVisit.containsKey(targetVertex)&& backVisit.containsKey(sourceVertex)) {
					backVisit.put(targetVertex, sourceVertex); //aggiungo alla mappa l'informazione
					//caso del grafo non orientato il padre viene dopo il figlio
				} else if(!backVisit.containsKey(sourceVertex)&& backVisit.containsKey(targetVertex)) {
					backVisit.put(sourceVertex, targetVertex);
				}
				
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Fermata> arg0) {
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Fermata> arg0) {
			}
		}); */ 
		
		backVisit.put(source, null); //inserisco il vertice di partenza che non ha un padre
		
		//faccio lavorare l'iteratore
		while(it.hasNext()) {
			result.add(it.next());
		}
		
//		System.out.println(backVisit);
		
		return result;
	}
	
	


	public List<Fermata> percorsoFinoA(Fermata target){
		if(!backVisit.containsKey(target)) { //target non è nella mapppa
			//il target non è raggiungibile dalla source 
			return null;
		}
		
		List<Fermata> percorso = new LinkedList<>();
		Fermata f = target;
		
		//ripercorro all'indietro l'albero per costruire una lista di fermate 
		while(f != null) {
			percorso.add(0,f);  //metto lo zero per indicare l'indice della lista in cui
								//inserire f in modo tale che sia in ordine
			f = backVisit.get(f);
		}
		
		return percorso;
		
	}

	public Graph<Fermata, DefaultWeightedEdge> getGrafo() {
		return grafo;
	}

	public List<Fermata> getFermate() {
		return fermate;
	}


	public List<Fermata> trovaCamminoMinimo(Fermata partenza, Fermata arrivo){
		DijkstraShortestPath<Fermata, DefaultWeightedEdge> dijstra = new DijkstraShortestPath<>(this.grafo);
		GraphPath<Fermata, DefaultWeightedEdge> path = dijstra.getPath(partenza, arrivo);
		return path.getVertexList();
		
	}


	
	
	
}
