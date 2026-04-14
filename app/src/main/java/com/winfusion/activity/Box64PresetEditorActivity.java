package com.winfusion.activity;

import androidx.appcompat.app.AppCompatActivity;

public class Box64PresetEditorActivity extends AppCompatActivity {

    // TODO: 实现box64预设编辑器
    // 将box64预设选择页面和overlay选择页面进行一个抽象，继续使用fragment
    // ok 对所有recyclerView滚动时隐藏右下角按钮的功能进行抽象，抽象到UiUtils
    // 预设编辑器第一页是可视化编辑，第二页是纯ini编辑
    // 对于可视化编辑，我们根据Key匹配我们的预设值，但是要允许意外的预设值存在而不报错
    // 对于可视化编辑器，我们需要给出一些Key的文档来帮助用户理解
    // 设计这整个的部分时要考虑到抽象，因为这部分几乎肯定会在未来的mangohud或者dxvk里面复用
    // 同时也要考虑到被box64rc部分复用的可能性

    // 正在完全重构settings模块
    // 所有equals的实现需要修改，需要改成:
    //    if (this == obj) {
    //        return true;
    //    }
    //    if (obj == null || getClass() != obj.getClass()) {
    //        return false;
    //    }
    //    // 接下来是字段对比
    // 同时注意实现了equals的类还需要实现hashcode

    // ok 设计一个类，用于提供所有配置的获取和设置接口，代替使用Key操作Config
    // 将Weston和WestonActivity结合，通过某种方式，让Weston从WestonActivity获得，而不是外部创建，这能够很好的解决Weston中的生命周期问题
    // ok 将OverlayController和OverlayView结合，通过某种方式，让OverlayController从OverlayView获得，而不是外部创建，者能够简化Overlay的使用
}
