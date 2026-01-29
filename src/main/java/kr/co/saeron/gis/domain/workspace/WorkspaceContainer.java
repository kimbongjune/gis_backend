package kr.co.saeron.gis.domain.workspace;

import java.util.List;

public class WorkspaceContainer {
    
    private List<WorkspaceItem> workspace;

    public List<WorkspaceItem> getWorkspace() { 
    		return workspace; 
    }
    public void setWorkspace(List<WorkspaceItem> workspace) {
    	this.workspace = workspace; 
    }
}
