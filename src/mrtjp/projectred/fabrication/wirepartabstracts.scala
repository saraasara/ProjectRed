/*
 * Copyright (c) 2015.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.projectred.fabrication

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import mrtjp.projectred.fabrication.IWireICPart._
import net.minecraft.nbt.NBTTagCompound

abstract class WireICPart extends CircuitPart with TConnectableICPart with TPropagatingICPart
{
    override def save(tag:NBTTagCompound)
    {
        tag.setByte("connMap", connMap)
    }

    override def load(tag:NBTTagCompound)
    {
        connMap = tag.getByte("connMap")
    }

    override def writeDesc(out:MCDataOutput)
    {
        out.writeByte(connMap)
    }

    override def readDesc(in:MCDataInput)
    {
        connMap = in.readByte()
    }

    override def read(in:MCDataInput, key:Int) = key match
    {
        case 1 => connMap = in.readByte()
        case _ => super.read(in, key)
    }

    def sendConnUpdate()
    {
        writeStreamOf(1).writeByte(connMap)
    }

    override def onMaskChanged(){ sendConnUpdate() }

    override def onNeighborChanged()
    {
        if (!world.network.isRemote)
        {
            ICPropagator.logCalculation()
            if (updateConns())
            {
                sendConnUpdate()
                ICPropagator.propagateTo(this, FORCE)
            }
            else ICPropagator.propagateTo(this, RISING)
        }
    }

    override def onAdded()
    {
        super.onAdded()
        if (!world.network.isRemote)
        {
            if (updateConns()) sendConnUpdate()
            ICPropagator.propagateTo(this, RISING)
        }
    }

    override def onRemoved()
    {
        super.onRemoved()
        if (!world.network.isRemote) notify(connMap)
    }

    override def diminishOnSide(r:Int) = true

    override def onSignalUpdate() =
        world.network.markDirty()
}