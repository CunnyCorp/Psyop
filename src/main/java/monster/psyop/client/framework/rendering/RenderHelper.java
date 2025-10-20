package monster.psyop.client.framework.rendering;

import java.util.ArrayList;
import java.util.List;

public class RenderHelper {
    public static void removeExpiredContracting(List<ContractingBlock> contractingBlocks) {
        List<ContractingBlock> toRemove = new ArrayList<>();

        for (ContractingBlock contractingBlock : contractingBlocks) {
            if (contractingBlock.isExpired()) {
                toRemove.add(contractingBlock);
            }
        }

        contractingBlocks.removeAll(toRemove);
    }
}
