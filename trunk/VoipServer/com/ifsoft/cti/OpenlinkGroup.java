package com.ifsoft.cti;

import org.jivesoftware.util.Log;
import java.util.*;

public class OpenlinkGroup
{
	private String groupName 			= null;
	private String groupGroupID			= null;
    private Map<String, OpenlinkGroupMember> groupMembers;

    public OpenlinkGroup()
    {
        groupMembers = Collections.synchronizedMap( new HashMap<String, OpenlinkGroupMember>());
    }

	public String getName()
	{
		return groupName;
	}

	public void setName(String groupName)
	{
		this.groupName = groupName;
	}

	public String getGroupID()
	{
		return groupGroupID;
	}

	public void setGroupID(String groupGroupID)
	{
		this.groupGroupID = groupGroupID;
	}

	public OpenlinkGroupMember getMember(String ID)
	{
		return groupMembers.get(ID);
	}

	public boolean isMember(String ID)
	{
		return groupMembers.containsKey(ID);
	}

	public void addMember(String ID, OpenlinkGroupMember member)
	{
		groupMembers.put(ID, member);
	}
}

