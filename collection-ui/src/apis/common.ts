// common.js

import { initInstance, getContainer } from "./utils";
import {render} from "vue";

const instanceMap = new Map();

export const showDialog = (dialog:object, option:object, call:Function) => {
    const container = getContainer();
    const opt = {
        ...option,
        onConfirm: (data:any) => {
            call(data);
        },
        onVanish: () => {
            render(null, container);
            instanceMap.delete(vm);
        },
    };
    const component = initInstance(dialog, container, opt);
    const vm = component!.proxy;
    component!.exposed!.openDialog();
    instanceMap.set(vm, { option: opt });
};
