package org.gcube.dataanalysis.lexicalmatcher.analysis.guesser.treeStructure.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JApplet;
import javax.swing.JFrame;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;

import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.DefaultEdge;


public class GraphGeneratorApplet extends JApplet {
	private static final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFF");
	private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);

	//
	private JGraphModelAdapter m_jgAdapter;

	/**
	 * @see java.applet.Applet#init().
	 */
	public void init() {
		// create a JGraphT graph
		ListenableGraph g = new ListenableDirectedGraph(DefaultEdge.class);

		// create a visualization using JGraph, via an adapter
		m_jgAdapter = new JGraphModelAdapter(g);

		JGraph jgraph = new JGraph(m_jgAdapter);

		adjustDisplaySettings(jgraph);
		getContentPane().add(jgraph);
		resize(DEFAULT_SIZE);

		// add some sample data (graph manipulated via JGraphT)
		g.addVertex("v1");
		g.addVertex("v2");
		g.addVertex("v3");
		g.addVertex("v4");

		g.addEdge("v1", "v2");
		g.addEdge("v2", "v3");
		g.addEdge("v3", "v1");
		g.addEdge("v4", "v3");

		
		
		// position vertices nicely within JGraph component
		positionVertexAt("v1", 130, 40);
		positionVertexAt("v2", 60, 200);
		positionVertexAt("v3", 310, 230);
		positionVertexAt("v4", 380, 70);

		// that's all there is to org.gcube.contentmanagement.lexicalmatcher!...
	}

	private void adjustDisplaySettings(JGraph jg) {
		jg.setPreferredSize(DEFAULT_SIZE);

		Color c = DEFAULT_BG_COLOR;
		String colorStr = null;

		try {
			colorStr = getParameter("bgcolor");
		} catch (Exception e) {
		}

		if (colorStr != null) {
			c = Color.decode(colorStr);
		}

		jg.setBackground(c);
	}

	private void positionVertexAt(Object vertex, int x, int y) {
		
		
		//seleziono la cella chiamata vertex
		DefaultGraphCell cell = m_jgAdapter.getVertexCell(vertex);
		//recupero gli attributi della cella
		Map attr = cell.getAttributes();
		//recupero i boundaries della cella
		Rectangle2D b = GraphConstants.getBounds(attr);
		//setto i parametri del nuovo rettangolo
		GraphConstants.setBounds(attr, new Rectangle(x, y, (int)b.getWidth(), (int)b.getHeight()));
		//costruisco una nuova cella
		Map cellAttr = new HashMap();
		cellAttr.put(cell, attr);
		//posiziono la cella nel grafo
		m_jgAdapter.edit(cellAttr, null, null, null);
		
	}
}