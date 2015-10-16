package nodes4j.core;

public enum ActorMessageTag {
	DATA, TASK, REDUCE, RESULT, SHUTDOWN;
	
	public ActorMessageTag valueOf(int tag) {
		return values()[tag];
	}
}
