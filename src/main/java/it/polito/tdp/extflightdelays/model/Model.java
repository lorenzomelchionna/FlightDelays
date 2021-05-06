package it.polito.tdp.extflightdelays.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {
	
	private SimpleWeightedGraph<Airport,DefaultWeightedEdge> Grafo;
	
	private ExtFlightDelaysDAO dao;
	
	private Map<Integer,Airport> idMap;
	
	private Map<Airport,Airport> visita;
	
	public Model() {
		
		dao = new ExtFlightDelaysDAO();
		
		idMap = new HashMap<>();
		dao.loadAllAirports(idMap);
		
	}
	
	public void creaGrafo(int x) {
		
		Grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		Graphs.addAllVertices(Grafo, dao.getVertici(x, idMap));
		
		for(Rotta r : dao.getRotte(idMap)) {
			
			if(this.Grafo.containsVertex(r.getA1()) && this.Grafo.containsVertex(r.getA2())) {
				
				DefaultWeightedEdge e = this.Grafo.getEdge(r.getA1(), r.getA2());
				
				if(e == null) 
					Graphs.addEdge(this.Grafo, r.getA1(), r.getA2(), r.getN());
				else {
					
					double pesoVecchio = this.Grafo.getEdgeWeight(e);
					double pesoNuovo = pesoVecchio + r.getN();
					this.Grafo.setEdgeWeight(e, pesoNuovo);
					
				}
				
			}
			
		}
		
		System.out.println("GRAFO CREATO!");
		System.out.println("# Vertici: " + Grafo.vertexSet().size()+"\n");
		System.out.println("# Archi: "+Grafo.edgeSet().size()+"\n");
		
	}

	public Set<Airport> getVertici() {
		
		if(Grafo != null)
			return Grafo.vertexSet();
		
		return null;
		
	}
	
	public int getNVertici() {
		
		if(Grafo != null)
			return Grafo.vertexSet().size();
		
		return 0;
		
	}
	
	public int getNArchi() {
		
		if(Grafo != null)
			return Grafo.edgeSet().size();
		
		return 0;
		
	}
	
	public List<Airport> trovaPercorso(Airport a1, Airport a2) {
		
		List<Airport> percorso = new LinkedList<>();
		
		BreadthFirstIterator<Airport,DefaultWeightedEdge> it = new BreadthFirstIterator<>(Grafo, a1);
		
		visita = new HashMap<>();
		visita.put(a1, null);
		
		it.addTraversalListener(new TraversalListener<Airport,DefaultWeightedEdge>(){

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {	
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				
				Airport airport1 = Grafo.getEdgeSource(e.getEdge());
				Airport airport2 = Grafo.getEdgeTarget(e.getEdge());
				
				if(visita.containsKey(airport1) && !visita.containsKey(airport2)) 
					visita.put(airport2, airport1);
				else if(visita.containsKey(airport2) && !visita.containsKey(airport1)) 
					visita.put(airport1, airport2);
				
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
			}
			
		});
		
		while(it.hasNext()) 
			it.next();
		
		if(!visita.containsKey(a1) || !visita.containsKey(a2)) {
			return null;
		}
		
		percorso.add(a2);
		
		Airport step = a2;
		
		while(visita.get(step) != null) {
			
			step = visita.get(step);
			percorso.add(0,step);
			
		}
		
		return percorso;
		
	}
	
}
