package graphics;

public class Render {

	public final int width;
	public final int height;
	public final int[] pixels;

	public Render(int width, int height) {
		this.width = width;
		this.height = height;
		pixels = new int[width * height];
	}

	public void draw(Render render, int xOffset, int yOffset) {
		for (int y = 0; y < render.height; y++) {
			int ypix = y + yOffset;
			for (int x = 0; x < render.width; x++) {
				int xpix = x + xOffset;

				pixels[xpix + ypix * width] = render.pixels[x + y * render.width];
			}
		}
	}

}