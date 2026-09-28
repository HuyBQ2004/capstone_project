# Integration Test Case — Manual Template (based on Report5_Test Report.xlsx)

> Copy the block below for each **Feature** (module) to be tested. Fill in manually following the guide at the bottom.
> Group test cases under a **Function** row, one row per test case, same pattern as the original `Feature N` sheet.

---

## Feature: `<FeatureName>`

### 1. General Information

| Field | Value |
|---|---|
| Feature | `<FeatureName>` |
| Test requirement | `<Brief description of the requirements tested in this sheet>` |
| Number of TCs | `<TotalTestCases>` |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | `<#>` | `<#>` | `<#>` | `<#>` |
| Round 2 | `<#>` | `<#>` | `<#>` | `<#>` |
| Round 3 | `<#>` | `<#>` | `<#>` | `<#>` |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Function A** | | | | | | | | | | | | | | |
| `<ID1>` | `<Brief description of this case: what is tested?>`<br>Ex: Test viewing "Company" form. | `<Describe steps to perform this case>`<br>Ex:<br>1. Login the system with Manager role.<br>2. Click "Company" tab in the left menu. | `<Describe results which meet customer's requirement>`<br>Ex: The "Company" view form is displayed with the following information:<br>- Company name<br>- Company address<br>- Phone<br>- Fax | `<List all test cases or conditions that must be done before performing this case>` | Pending | | | Pending | | | Pending | | | |
| `<ID2>` | `<Test case 2>` | | | | Pending | | | Pending | | | Pending | | | |
| `<ID3>` | `<Test case 3>` | | | | Pending | | | Pending | | | Pending | | | |
| **Function B** | | | | | | | | | | | | | | |
| `<ID4>` | `<Test case 4>` | | | | Pending | | | Pending | | | Pending | | | |

---

## Filling Guide (condensed from the original template)

1. **Feature** = the module/feature under test (maps to one `Feature N` sheet in the Excel template, e.g. MF02, MF03...).
2. **Number of TCs** = total count of test case rows in section 3 (excluding the `Function X` group header rows).
3. Group test cases by **Function** (a bold header row with only the function name, no other columns filled), then list each test case below it.
4. **Test Case Description**: what is being tested, in one sentence.
5. **Test Case Procedure**: numbered steps to execute the case.
6. **Expected Results**: the outcome that must be observed for the case to Pass.
7. **Pre-conditions**: state/data that must exist before running the case.
8. **Round 1/2/3**: status per execution round — `Pending` until run, then `Passed` or `Failed`; fill **Test date** and **Tester** when executed.
9. **Note**: any extra remark (e.g. linked defect ID, method name in code, blocked reason).
10. Update section 2 (Testing Round Summary) by counting the actual Passed/Failed/Pending/N-A values from section 3 for each round — in Excel this uses `COUNTIF` formulas; here, fill them in manually.
