import { useConfirm } from "primevue/useconfirm";
import { useToast } from "primevue/usetoast";

export function useDeleteConfirm() {
  const confirm = useConfirm();
  const toast = useToast();

  const confirmDelete = (itemName, deleteAction, options = {}) => {
    const {
      message = `Are you sure you want to delete "${itemName}"?`,
      header = "Confirmation",
      icon = "pi pi-exclamation-triangle text-red-500",
      acceptLabel = "Yes",
      rejectLabel = "No",
      successMessage = `${itemName} deleted successfully`,
      errorMessage = "Delete failed",
      onSuccess = null, // 成功后的回调
      onError = null, // 失败后的回调
    } = options;

    confirm.require({
      message,
      header,
      icon,
      acceptLabel,
      rejectLabel,
      acceptProps: { class: "p-button-success", icon: "pi pi-check" }, // 绿色
      rejectProps: { class: "p-button-danger", icon: "pi pi-times" }, // 红色
      accept: async () => {
        try {
          await deleteAction();
          toast.add({
            severity: "success",
            summary: "Success",
            detail: successMessage,
            life: 3000,
          });
          if (onSuccess) onSuccess();
        } catch (err) {
          toast.add({
            severity: "error",
            summary: "Error",
            detail: err.message || errorMessage,
            life: 3000,
          });
          if (onError) onError(err);
        }
      },
      reject: () => {},
    });
  };

  return { confirmDelete };
}
