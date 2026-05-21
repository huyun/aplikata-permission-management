export function showRequiredMessage(event, toast) {
  event.preventDefault();
  const input = event.target;
  const label = document.querySelector(`label[for="${input.id}"]`);
  let fieldName = "This field";
  if (label) {
    let rawText = label.innerText.trim();
    fieldName = rawText.replace(/\s*\*$/, "").trim();
  }
  toast.add({
    severity: "error",
    summary: "Validation Error",
    detail: `${fieldName} is required!`,
    life: 3000,
  });
  // 可选：添加错误边框样式
  input.classList.add("user-invalid");
  input.addEventListener(
    "input",
    () => input.classList.remove("user-invalid"),
    { once: true },
  );
}
