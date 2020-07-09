package org.eastsideprep.javaneutrons.core;

import javafx.scene.Group;

public class AssemblyGroup extends Group{
    public Assembly assembly;
    
    public AssemblyGroup(Assembly assembly) {
        this.assembly = assembly;
    }
}
