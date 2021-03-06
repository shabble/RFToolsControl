package mcjty.rftoolscontrol.blocks.processor;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.GenericRFToolsBlock;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ProcessorBlock extends GenericRFToolsBlock<ProcessorTileEntity, ProcessorContainer> {

    @Override
    public boolean needsRedstoneCheck() {
        return true;
    }

    public ProcessorBlock() {
        super(Material.IRON, ProcessorTileEntity.class, ProcessorContainer.class, "processor", false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initModel() {
        ClientRegistry.bindTileEntitySpecialRenderer(ProcessorTileEntity.class, new ProcessorRenderer());
        super.initModel();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiProcessor.class;
    }

    @Override
    public int getGuiID() {
        return RFToolsControl.GUI_PROCESSOR;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> list, boolean advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("The processor executes programs");
        list.add("for automation");
    }

    @Override
    protected void clOnNeighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn) {
        super.clOnNeighborChanged(state, world, pos, blockIn);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ProcessorTileEntity) {
            ((ProcessorTileEntity) te).markFluidSlotsDirty();
        }
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(world, pos, neighbor);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ProcessorTileEntity) {
            ((ProcessorTileEntity) te).markFluidSlotsDirty();
        }
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof ProcessorTileEntity) {
            ProcessorTileEntity processor = (ProcessorTileEntity) te;
            if (processor.hasNetworkCard()) {
                probeInfo.text(TextFormatting.GREEN + "Channel: " + processor.getChannelName());
                probeInfo.text(TextFormatting.GREEN + "Nodes: " + processor.getNodeCount());
            }
            if (mode == ProbeMode.EXTENDED) {
                List<String> lastMessages = processor.getLastMessages(6);
                if (!lastMessages.isEmpty()) {
                    IProbeInfo v = probeInfo.vertical(probeInfo.defaultLayoutStyle().borderColor(0xffff0000));
                    for (String s : lastMessages) {
                        v.text("    " + s);
                    }
                }
            }
        }
    }


    private int getInputStrength(World world, BlockPos pos, EnumFacing side) {
        return world.getRedstonePower(pos.offset(side), side);
    }

    @Override
    protected void checkRedstone(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        TileEntity te = world.getTileEntity(pos);
        if (state.getBlock() instanceof ProcessorBlock && te instanceof ProcessorTileEntity) {
            ProcessorTileEntity processor = (ProcessorTileEntity)te;
            int powered = 0;
            if (getInputStrength(world, pos, EnumFacing.DOWN) > 0) {
                powered += 1;
            }
            if (getInputStrength(world, pos, EnumFacing.UP) > 0) {
                powered += 2;
            }
            if (getInputStrength(world, pos, EnumFacing.NORTH) > 0) {
                powered += 4;
            }
            if (getInputStrength(world, pos, EnumFacing.SOUTH) > 0) {
                powered += 8;
            }
            if (getInputStrength(world, pos, EnumFacing.WEST) > 0) {
                powered += 16;
            }
            if (getInputStrength(world, pos, EnumFacing.EAST) > 0) {
                powered += 32;
            }
            processor.setPowerInput(powered);
        }
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    protected int getRedstoneOutput(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity te = world.getTileEntity(pos);
        if (state.getBlock() instanceof ProcessorBlock && te instanceof ProcessorTileEntity) {
            ProcessorTileEntity processor = (ProcessorTileEntity) te;
            return processor.getPowerOut(side.getOpposite());
        }
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

}
