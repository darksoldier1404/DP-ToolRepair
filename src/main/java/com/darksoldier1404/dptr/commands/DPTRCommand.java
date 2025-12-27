package com.darksoldier1404.dptr.commands;

import com.darksoldier1404.dppc.builder.command.CommandBuilder;
import com.darksoldier1404.dptr.functions.DPTRFunction;

import static com.darksoldier1404.dptr.ToolRepair.plugin;


public class DPTRCommand {
    private static final CommandBuilder builder = new CommandBuilder(plugin);

    public static void init() {
        builder.beginSubCommand("open", "/dptr open - 도구 수리 GUI 열기")
                .withPermission("dptr.use")
                .executesPlayer((p, args) -> {
                    DPTRFunction.openToolRepairGUI(p);
                    return true;
                });
        builder.beginSubCommand("reload", "/dptr reload - 설정 파일 다시 불러오기")
                .withPermission("dptr.admin")
                .executes((sender, args) -> {
                    plugin.reload();
                    DPTRFunction.init();
                    sender.sendMessage(plugin.getPrefix() + "§a설정이 다시 불러와졌습니다.");
                    return true;
                });
        builder.build("dptr");
    }
}