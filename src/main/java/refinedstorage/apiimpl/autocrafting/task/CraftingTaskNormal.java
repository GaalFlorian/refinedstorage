package refinedstorage.apiimpl.autocrafting.task;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import refinedstorage.api.autocrafting.ICraftingPattern;
import refinedstorage.api.network.INetworkMaster;
import refinedstorage.api.network.NetworkUtils;

public class CraftingTaskNormal extends CraftingTask {
    public static final String NBT_SATISFIED = "Satisfied";
    public static final String NBT_CHECKED = "Checked";

    private boolean satisfied[];
    private boolean checked[];

    public CraftingTaskNormal(ICraftingPattern pattern) {
        super(pattern);

        this.satisfied = new boolean[pattern.getInputs().length];
        this.checked = new boolean[pattern.getInputs().length];
    }

    public void setSatisfied(boolean[] satisfied) {
        this.satisfied = satisfied;
    }

    public void setChecked(boolean[] checked) {
        this.checked = checked;
    }

    @Override
    public ICraftingPattern getPattern() {
        return pattern;
    }

    @Override
    public boolean update(World world, INetworkMaster network) {
        for (int i = 0; i < pattern.getInputs().length; ++i) {
            checked[i] = true;

            ItemStack input = pattern.getInputs()[i];

            if (!satisfied[i]) {
                ItemStack received = NetworkUtils.extractItem(network, input, input.stackSize);

                if (received != null) {
                    satisfied[i] = true;

                    took.add(received);

                    network.updateCraftingTasks();
                } else {
                    tryCreateChild(network, i);
                }

                break;
            }
        }

        for (boolean item : satisfied) {
            if (!item) {
                return false;
            }
        }

        for (ItemStack output : pattern.getOutputs()) {
            // @TODO: Handle remainder
            network.insertItem(output, output.stackSize, false);
        }

        if (pattern.getByproducts() != null) {
            for (ItemStack byproduct : pattern.getByproducts()) {
                // @TODO: Handle remainder
                network.insertItem(byproduct, byproduct.stackSize, false);
            }
        }

        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        writeBooleanArray(tag, NBT_SATISFIED, satisfied);
        writeBooleanArray(tag, NBT_CHECKED, checked);

        return tag;
    }

    @Override
    public String getStatus() {
        StringBuilder builder = new StringBuilder();

        boolean missingItems = false;

        for (int i = 0; i < pattern.getInputs().length; ++i) {
            ItemStack input = pattern.getInputs()[i];

            if (!satisfied[i] && !childrenCreated[i] && checked[i]) {
                if (!missingItems) {
                    builder.append("I=gui.refinedstorage:crafting_monitor.missing_items\n");

                    missingItems = true;
                }

                builder.append("T=").append(input.getUnlocalizedName()).append(".name\n");
            }
        }

        boolean itemsCrafting = false;

        for (int i = 0; i < pattern.getInputs().length; ++i) {
            ItemStack input = pattern.getInputs()[i];

            if (!satisfied[i] && childrenCreated[i] && checked[i]) {
                if (!itemsCrafting) {
                    builder.append("I=gui.refinedstorage:crafting_monitor.items_crafting\n");

                    itemsCrafting = true;
                }

                builder.append("T=").append(input.getUnlocalizedName()).append(".name\n");
            }
        }

        return builder.toString();
    }
}
