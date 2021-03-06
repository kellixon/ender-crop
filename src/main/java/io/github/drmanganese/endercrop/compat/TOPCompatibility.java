package io.github.drmanganese.endercrop.compat;

import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import io.github.drmanganese.endercrop.block.BlockCropEnder;
import io.github.drmanganese.endercrop.block.BlockTilledEndStone;
import io.github.drmanganese.endercrop.init.ModBlocks;
import io.github.drmanganese.endercrop.reference.Reference;

import com.google.common.base.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;

public class TOPCompatibility {
    private static boolean registered;

    public static void register() {
        if (registered)
            return;
        registered = true;
        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "io.github.drmanganese.endercrop.compat.TOPCompatibility$GetTheOneProbe");
    }

    public static class GetTheOneProbe implements Function<ITheOneProbe, Void> {
        public static ITheOneProbe probe;
        @Nullable
        @Override
        public Void apply(ITheOneProbe theOneProbe) {
            probe = theOneProbe;
            probe.registerProvider(new IProbeInfoProvider() {
                @Override
                public String getID() {
                    return Reference.MOD_ID;
                }

                @Override
                public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
                    if (blockState.getBlock() instanceof BlockTilledEndStone) {
                        if (mode == ProbeMode.EXTENDED) {
                            if (blockState.getValue(BlockTilledEndStone.MOISTURE) == 7) {
                                probeInfo.text(TextFormatting.DARK_GRAY + "Moist");
                            } else {
                                probeInfo.text(TextFormatting.DARK_GRAY + "Dry");
                            }
                        }

                        if (mode == ProbeMode.DEBUG) {
                            probeInfo.text("MOISTURE: " + blockState.getValue(BlockTilledEndStone.MOISTURE));
                        }
                    } else if (blockState.getBlock() == ModBlocks.CROP_ENDER) {
                        float age = blockState.getValue(BlockCropEnder.AGE) / 7.0F;

                        if (age < 1.0F) {
                            if (world.getBlockState(data.getPos().down()).getBlock() == Blocks.FARMLAND && !ModBlocks.CROP_ENDER.canGrow(world, data.getPos(), blockState, world.isRemote)) {
                                probeInfo.text(TextFormatting.RED + "Can't grow");
                            }
                        }
                        if (mode != ProbeMode.NORMAL) {
                            String text = TextFormatting.YELLOW + "Light" + ": " + world.getLightFromNeighbors(data.getPos().up());
                            if (world.getLightFromNeighbors(data.getPos().up()) >= 7)
                                text += TextFormatting.RED + "(>=7)";
                            probeInfo.text(text);
                        }
                    } else if (blockState.getBlock() == Blocks.END_STONE) {
                        if (hoeInHand(player.getHeldItemMainhand()) || hoeInHand(player.getHeldItemOffhand())) {
                            if (player.isCreative() || canHoeEndStone(player.getHeldItemMainhand()) || canHoeEndStone(player.getHeldItemOffhand())) {
                                probeInfo.text(TextFormatting.GREEN+ "\u2714" + "Can hoe");
                            } else {
                                probeInfo.text(TextFormatting.RED + "\u2718" + "Can't hoe, enchant with Unbreaking I+");
                            }
                        }

                    }
                }

            });
            return null;
        }

        private boolean canHoeEndStone(@Nonnull ItemStack stack) {
            return hoeInHand(stack) && EnchantmentHelper.getEnchantmentLevel(Enchantment.getEnchantmentByID(34), stack) > 0;
        }

        private boolean hoeInHand(@Nonnull ItemStack stack) {
            return !stack.isEmpty() && stack.getItem() instanceof ItemHoe;
        }
    }
}
