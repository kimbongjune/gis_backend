package kr.co.saeron.gis.domain.workspace;

import java.util.List;

public class WorkspaceListResponse {
    
    private WorkspaceContainer workspaces;

    public WorkspaceContainer getWorkspaces() { 
    	return workspaces; 
    }
    
    public void setWorkspaces(WorkspaceContainer workspaces) {
    	this.workspaces = workspaces;
    }
}
