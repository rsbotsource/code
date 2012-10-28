package org.powerbot.game.client;

public class ModelCapture implements Model {
	private int[] vertex_x;
	private int[] vertex_y;
	private int[] vertex_z;

	private short[] face_a;
	private short[] face_b;
	private short[] face_c;
	private int numVertices;
	private int numFaces;

	private ModelCapture(final Model model) {
		if (model == null) {
			return;
		}
		vertex_x = model.getXPoints().clone();
		vertex_y = model.getYPoints().clone();
		vertex_z = model.getZPoints().clone();
		numVertices = Math.min(vertex_x.length, Math.min(vertex_y.length, vertex_z.length));

		face_a = model.getIndices1().clone();
		face_b = model.getIndices2().clone();
		face_c = model.getIndices3().clone();
		numFaces = Math.min(face_a.length, Math.min(face_b.length, face_c.length));
	}

	public static Model updateModel(final Model game, Model capture) {
		if (capture == null || !(capture instanceof ModelCapture)) {
			capture = new ModelCapture(game);
			return capture;
		}
		final ModelCapture reused_capture = (ModelCapture) capture;
		reused_capture.update(game);
		return reused_capture;
	}

	private void update(final Model model) {
		if (model == null) {
			return;
		}

		final int[] vertices_x = model.getXPoints();
		final int[] vertices_y = model.getYPoints();
		final int[] vertices_z = model.getZPoints();
		final short[] indices1 = model.getIndices1();
		final short[] indices2 = model.getIndices2();
		final short[] indices3 = model.getIndices3();
		final int numVertices = Math.min(vertices_x.length, Math.min(vertices_y.length, vertices_z.length));
		final int numFaces = Math.min(indices1.length, Math.min(indices2.length, indices3.length));
		if (numVertices > this.numVertices) {
			this.numVertices = numVertices;
			vertex_x = vertices_x.clone();
			vertex_y = vertices_y.clone();
			vertex_z = vertices_z.clone();
		} else {
			this.numVertices = numVertices;
			System.arraycopy(vertices_x, 0, vertex_x, 0, numVertices);
			System.arraycopy(vertices_y, 0, vertex_y, 0, numVertices);
			System.arraycopy(vertices_z, 0, vertex_z, 0, numVertices);
		}

		if (numFaces > this.numFaces) {
			this.numFaces = numFaces;
			face_a = indices1.clone();
			face_b = indices2.clone();
			face_c = indices3.clone();
		} else {
			this.numFaces = numFaces;
			System.arraycopy(indices1, 0, face_a, 0, numFaces);
			System.arraycopy(indices2, 0, face_b, 0, numFaces);
			System.arraycopy(indices3, 0, face_c, 0, numFaces);
		}
	}

	public int[] getXPoints() {
		return vertex_x;
	}

	public int[] getYPoints() {
		return vertex_y;
	}

	public int[] getZPoints() {
		return vertex_z;
	}

	public short[] getIndices1() {
		return face_a;
	}

	public short[] getIndices2() {
		return face_b;
	}

	public short[] getIndices3() {
		return face_c;
	}

	public int getNumVertices() {
		return numVertices;
	}

	public int getNumFaces() {
		return numFaces;
	}
}
