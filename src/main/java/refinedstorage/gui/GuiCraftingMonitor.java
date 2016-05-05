package refinedstorage.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import refinedstorage.container.ContainerCraftingMonitor;
import refinedstorage.gui.sidebutton.SideButtonRedstoneMode;
import refinedstorage.tile.autocrafting.TileCraftingMonitor;
import scala.actors.threadpool.Arrays;

import java.util.List;

public class GuiCraftingMonitor extends GuiBase {
    public static final int VISIBLE_ROWS = 3;

    private TileCraftingMonitor craftingMonitor;

    private Scrollbar scrollbar = new Scrollbar(157, 20, 12, 89);

    public GuiCraftingMonitor(ContainerCraftingMonitor container, TileCraftingMonitor craftingMonitor) {
        super(container, 176, 211);

        this.craftingMonitor = craftingMonitor;
    }

    @Override
    public void init(int x, int y) {
        addSideButton(new SideButtonRedstoneMode(craftingMonitor));
    }

    @Override
    public void update(int x, int y) {
        scrollbar.setCanScroll(getRows() > VISIBLE_ROWS);
        scrollbar.setScrollDelta((float) scrollbar.getScrollbarHeight() / (float) getRows());
    }

    @Override
    public void drawBackground(int x, int y, int mouseX, int mouseY) {
        bindTexture("gui/crafting_monitor.png");

        drawTexture(x, y, 0, 0, width, height);

        scrollbar.draw(this);
    }

    private int calculateOffsetOnScale(int pos, float scale) {
        float multiplier = (pos / scale);
        return (int) multiplier;
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        scrollbar.update(this, mouseX, mouseY);

        drawString(7, 7, t("gui.refinedstorage:crafting_monitor"));
        drawString(7, 116, t("container.inventory"));

        int ox = 11;
        int x = ox;
        int y = 26;

        int slot = getOffset() * 2;

        RenderHelper.enableGUIStandardItemLighting();

        List<ItemStack> tasks = craftingMonitor.getTasks();

        List<String> infoLines = null;

        for (int i = 0; i < 6; ++i) {
            if (slot < tasks.size() && slot < craftingMonitor.getInfo().length) {
                ItemStack task = tasks.get(slot);

                drawItem(x, y + 5, task);

                GlStateManager.pushMatrix();

                float scale = 0.5f;
                GlStateManager.scale(scale, scale, 1);
                drawString(calculateOffsetOnScale(x + 1, scale), calculateOffsetOnScale(y - 3, scale), task.getDisplayName());

                GlStateManager.popMatrix();

                if (inBounds(x, y + 5, 16, 16, mouseX, mouseY)) {
                    infoLines = Arrays.asList(craftingMonitor.getInfo()[slot].split("\n"));

                    for (int j = 0; j < infoLines.size(); ++j) {
                        String line = infoLines.get(j);

                        infoLines.set(j, line
                            .replace("{missing_items}", t("gui.refinedstorage:crafting_monitor.missing_items"))
                            .replace("{items_crafting}", t("gui.refinedstorage:crafting_monitor.items_crafting"))
                            .replace("{items_processing}", t("gui.refinedstorage:crafting_monitor.items_processing"))
                            .replace("{none}", t("misc.refinedstorage:none")));
                    }
                }
            }

            if (i == 1 || i == 3) {
                x = ox;
                y += 30;
            } else {
                x += 75;
            }

            slot++;
        }

        if (infoLines != null) {
            drawTooltip(mouseX, mouseY, infoLines);
        }
    }

    public int getOffset() {
        return (int) (scrollbar.getCurrentScroll() / 89f * (float) getRows());
    }

    private int getRows() {
        int max = (int) Math.ceil((float) craftingMonitor.getTasks().size() / (float) 2);

        return max < 0 ? 0 : max;
    }
}
