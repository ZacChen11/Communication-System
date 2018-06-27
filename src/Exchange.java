import java.io.IOException;

public class Exchange {
	public static void main(String[] args) throws IOException {
		Master master = new Master("calls.txt");
		master.start();
	}
}
