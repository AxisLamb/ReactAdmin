// SysDictVO.java
package com.lain.modules.sys.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "数据字典VO")
public class SysDictVO {
    @Schema(description = "字典ID")
    private Long dictId;

    @Schema(description = "字典名称")
    private String dictName;

    @Schema(description = "字典类型")
    private String dictType;

    @Schema(description = "状态（0禁用 1正常）")
    private Integer status = 1;

    @Schema(description = "备注")
    private String remark;

}
