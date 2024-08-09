package com.syrency.mc.blockentities;

import com.syrency.mc.SyrencyMod;
import com.syrency.mc.blocks.GrowthDetectorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class GrowthDetectorBlockEntity extends BlockEntity implements Tickable {

    private int currentPower = 0;

    public GrowthDetectorBlockEntity() {
        super(SyrencyMod.GROWTH_DETECTOR_BLOCK_ENTITY);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);

        tag.putInt("currentPower", currentPower);

        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);

        currentPower = tag.getInt("currentPower");
    }

    @Override
    public void tick() {
        BlockState thisState = world.getBlockState(pos);
        Direction facing = thisState.get(Properties.FACING);
        BlockPos inFront = pos.add(facing.getVector());

        BlockState lookingState = world.getBlockState(inFront);

        Integer age = getBlockStateAge(lookingState);

        if (age != null) {
            int age1 = age.intValue() + 1;
            if (currentPower != age1) {
                currentPower = age1;
                world.setBlockState(pos, thisState.with(GrowthDetectorBlock.POWER, currentPower));
            }
        } else {
            if (currentPower != 0) {
                currentPower = 0;
                world.setBlockState(pos, thisState.with(GrowthDetectorBlock.POWER, currentPower));
            }
        }
    }

    private Integer getBlockStateAge(BlockState bs) {
        //age 1 bamboo
        //age 25 kelp / vines
        //age 15 sugarcane
        // ^^ the ages for these did not give anything useful

        if (bs.contains(Properties.AGE_2)) {
            return bs.get(Properties.AGE_2);
        } else if (bs.contains(Properties.AGE_3)) {
            return bs.get(Properties.AGE_3);
        } else if (bs.contains(Properties.AGE_5)) {
            return bs.get(Properties.AGE_5);
        } else if (bs.contains(Properties.AGE_7)) {
            return bs.get(Properties.AGE_7);
        } else if (bs.contains(Properties.STAGE)) {
            return bs.get(Properties.STAGE);
        } else {
            return null;
        }
    }
}
