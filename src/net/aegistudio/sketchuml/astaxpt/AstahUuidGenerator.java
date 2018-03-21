package net.aegistudio.sketchuml.astaxpt;

/**
 * Generate essential entity id for astah's entities.
 * 
 * @author Haoran Luo
 */
public interface AstahUuidGenerator {
	/**
	 * (This method should be implemented thread-safely)
	 * 
	 * @return the next string of representing a distinct id.
	 */
	public String nextUuid();
}
