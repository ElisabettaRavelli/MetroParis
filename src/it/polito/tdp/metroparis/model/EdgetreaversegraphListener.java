package it.polito.tdp.metroparis.model;

import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;

public class EdgetreaversegraphListener implements TraversalListener<Fermata,DefaultEdge> {
	
	Graph<Fermata, DefaultEdge> grafo;
	Map<Fermata, Fermata> back;
	
	public EdgetreaversegraphListener(Map<Fermata, Fermata> back, Graph<Fermata, DefaultEdge> grafo) {
		super();
		this.grafo = grafo;
		this.back = back;
	}

	@Override
	public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {
	
		
	}

	@Override
	public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {
	
		
	}

	@Override
	public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> ev) {
	/*
	 * LA VISITA DI UN ARCO DEVE PORTARE A QUALCOSA DI NUOVO PER APPARTENERE ALL'ALBERO DI VISITA
	 * back codifica relazioni del tipo child->parent
	 * per un nuovo vertice 'child' scoperto
	 * devo avere:
	 * - child ancora sconosciuto (non ancora trovato)
	 * - parent è già stato visitato 
	 */
		
		Fermata sourceVertex = grafo.getEdgeSource(ev.getEdge());
		Fermata targetVertex = grafo.getEdgeTarget(ev.getEdge());
		
		/*
		 * se il grafo è orientato, allora source==parent, target==child
		 * se il grafo non è orientato, potrebbe essere al contrario...
		 */
		
		//se il figlio (target) non è ancora nella mappa ma il padre (source) è nella mappa
		if(!back.containsKey(targetVertex)&& back.containsKey(sourceVertex)) {
			back.put(targetVertex, sourceVertex); //aggiungo alla mappa l'informazione
			//caso del grafo non orientato il padre viene dopo il figlio
		} else if(!back.containsKey(sourceVertex)&& back.containsKey(targetVertex)) {
			back.put(sourceVertex, targetVertex);
		}
		
	}

	@Override
	public void vertexFinished(VertexTraversalEvent<Fermata> arg0) {

		
	}

	@Override
	public void vertexTraversed(VertexTraversalEvent<Fermata> arg0) {
	
		
	}
	

}
