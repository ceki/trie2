package linearHashing;

public class Mod2N {

	
	public static void main(String[] args) {
		
		int L = 23;
		
		for(int k = 0; k < 2*L; k++) {
		
			int r2 = k % (2*L);
			int r1 = k % L;
			
			if(r2 == r1) {
			  System.out.println("Equality   " + r2 + " = " + k + " % " + (2*L) + " = " + k + " % " + (L));
			} else if(r2 == (r1 + L)) {
			  System.out.println("Equality+L " + r2 + " = " + k + " % " + (2*L) + " = " + k + " % " + (L) + " + " + L);
			} else {
				System.out.println("Unexpected result at k="+k);
				return;
			}
			
			
			
		}
		
	}
	
	
	int mod(int c, int N) {
		return c % N;
	}
}
