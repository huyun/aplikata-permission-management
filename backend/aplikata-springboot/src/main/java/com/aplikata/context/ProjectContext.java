package com.aplikata.context;

public class ProjectContext {
	private static final ThreadLocal<Long> currentProject = new ThreadLocal<>();

	public static void setProjectId(Long projectId) {
		currentProject.set(projectId);
	}

	public static Long getProjectId() {
		return currentProject.get();
	}

	public static void clear() {
		currentProject.remove();
	}
}
