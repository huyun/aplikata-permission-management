/**
 * 自动为所有 required 输入框关联的 label 添加红色星号
 */
export function markRequiredLabels() {
  document
    .querySelectorAll("input[required], select[required], textarea[required]")
    .forEach((el) => {
      const label = document.querySelector(`label[for="${el.id}"]`);
      if (label && !label.querySelector(".required-star")) {
        const star = document.createElement("span");
        star.className = "required-star";
        star.textContent = "*";
        star.style.color = "#f87274";
        star.style.marginLeft = "4px";
        label.appendChild(star);
      }
    });
}
